package io.github.mvillafuertem.aws.s3

import java.net.URI
import java.nio.file.Path
import java.util.concurrent.CompletionException

import io.github.mvillafuertem.aws.s3.S3ApplicationIT.S3ApplicationConfigurationIT
import io.github.mvillafuertem.aws.{ LocalStackConfigurationIT, RichS3AsyncClientBuilder }
import org.testcontainers.containers
import org.testcontainers.containers.wait.strategy.Wait
import software.amazon.awssdk.auth.credentials.{ AwsBasicCredentials, AwsCredentialsProvider, StaticCredentialsProvider }
import software.amazon.awssdk.http.HttpStatusCode
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model._

import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.{ ExecutionContext, Future }

final class S3ApplicationIT extends S3ApplicationConfigurationIT {

  behavior of s"${this.getClass.getSimpleName}"

  it should "Create Bucket Request" in {

    // g i v e n
    val NEW_BUCKET_NAME   = "new-bucket-test"
    val headObjectRequest = CreateBucketRequest
      .builder()
      .bucket(NEW_BUCKET_NAME)
      .build()

    // w h e n
    val createBucketResponse: Future[CreateBucketResponse] = for {
      _                  <- createBucketData()
      headObjectResponse <- s3AsyncClientDefault
                              .createBucket(headObjectRequest)
                              .toScala
                              .recover {
                                case e: CompletionException if e.getCause.isInstanceOf[NoSuchKeyException] => throw e
                                case e                                                                     => throw e
                              }
    } yield headObjectResponse

    // t h e n
    createBucketResponse.map { actual =>
      actual.sdkHttpResponse().statusCode() shouldBe HttpStatusCode.OK
    }

  }

  it should "Head Object Request" in {

    // g i v e n
    val headObjectRequest = HeadObjectRequest
      .builder()
      .bucket(BUCKET_NAME)
      .key(KEY)
      .build()

    // w h e n
    val headObjectResponse: Future[HeadObjectResponse] = for {
      _                  <- putObjectData()
      headObjectResponse <- s3AsyncClientDefault
                              .headObject(headObjectRequest)
                              .toScala
                              .recover {
                                case e: CompletionException if e.getCause.isInstanceOf[NoSuchKeyException] => throw e
                                case e                                                                     => throw e
                              }
    } yield headObjectResponse

    // t h e n
    headObjectResponse.map { actual =>
      actual.sdkHttpResponse().statusCode() shouldBe HttpStatusCode.OK
    }

  }

  it should "Get Object Request" in {

    // g i v e n
    val getObjectRequest = GetObjectRequest
      .builder()
      .bucket(BUCKET_NAME)
      .key(KEY)
      .build()

    // w h e n
    val getObjectResponse: Future[GetObjectResponse] = for {
      headObjectResponse <- s3AsyncClientDefault
                              .getObject(getObjectRequest, Path.of("/tmp/integration-test.xml"))
                              .toScala
                              .recover {
                                case e: CompletionException if e.getCause.isInstanceOf[NoSuchKeyException] => throw e
                                case e                                                                     => throw e
                              }
    } yield headObjectResponse

    // t h e n
    getObjectResponse.map { actual =>
      actual.sdkHttpResponse().statusCode() shouldBe HttpStatusCode.OK
    }

  }

  override var container: containers.DockerComposeContainer[_] = _

  override protected def beforeAll(): Unit = {
    container = dockerInfrastructure(Wait.forLogMessage(".*Starting mock S3 service.*\\n", 1))
    container.start()
  }

  override protected def afterAll(): Unit = container.stop()

}

object S3ApplicationIT {

  trait S3ApplicationConfigurationIT extends LocalStackConfigurationIT {

    val BUCKET_NAME: String = "bucket-test"
    val KEY                 = "logback-test.xml"

    val s3AsyncClientDefault: S3AsyncClient = s3AsyncClient(
      region = Some(Region.US_EAST_1),
      endpoint = Some(URI.create(AWS_LOCALSTACK_ENDPOINT)),
      credentialsProvider = Some(
        StaticCredentialsProvider.create(
          AwsBasicCredentials.create(
            "accessKey",
            "secretKey"
          )
        )
      )
    )

    def createBucketData()(implicit executionContext: ExecutionContext): Future[CreateBucketResponse] = {
      val createBucketRequest = CreateBucketRequest
        .builder()
        .bucket(BUCKET_NAME)
        .build()

      s3AsyncClientDefault
        .createBucket(createBucketRequest)
        .toScala
        .recover {
          case e: CompletionException if e.getCause.isInstanceOf[NoSuchKeyException] => throw e
          case e                                                                     => throw e
        }
    }

    def putObjectData()(implicit executionContext: ExecutionContext): Future[PutObjectResponse] = {
      val putObjectRequest = PutObjectRequest
        .builder()
        .bucket(BUCKET_NAME)
        .key(KEY)
        .acl("public-read")
        .build()
      s3AsyncClientDefault
        .putObject(putObjectRequest, Path.of(s"${System.getProperty("user.dir")}/modules/aws/src/it/resources/$KEY"))
        .toScala
        .recover {
          case e: CompletionException if e.getCause.isInstanceOf[NoSuchKeyException] => throw e
          case e                                                                     => throw e
        }
    }

    def s3AsyncClient(
      region: Option[Region] = None,
      endpoint: Option[URI] = None,
      credentialsProvider: Option[AwsCredentialsProvider] = None
    ): S3AsyncClient =
      S3AsyncClient
        .builder()
        .add(region, _.region)
        .add(endpoint, _.endpointOverride)
        .add(credentialsProvider, _.credentialsProvider)
        .build()

  }

}
