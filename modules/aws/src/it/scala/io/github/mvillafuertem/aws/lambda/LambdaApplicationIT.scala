package io.github.mvillafuertem.aws.lambda

import java.io.{File, FileInputStream}
import java.net.URI
import java.util

import com.dimafeng.testcontainers.{DockerComposeContainer, ExposedService}
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import io.github.mvillafuertem.aws.RichLambdaAsyncClientBuilder
import io.github.mvillafuertem.aws.lambda.LambdaApplicationIT.LambdaApplicationConfigurationIT
import io.github.mvillafuertem.aws.lambda.SampleLambda.WeatherData
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.testcontainers.containers
import org.testcontainers.containers.wait.strategy.Wait
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, AwsCredentialsProvider, StaticCredentialsProvider}
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.http.HttpStatusCode
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.lambda.LambdaAsyncClient
import software.amazon.awssdk.services.lambda.model.{InvokeResponse, _}

import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.Future
import scala.jdk.CollectionConverters.MapHasAsJava
import scala.sys.process._

final class LambdaApplicationIT extends AsyncFlatSpecLike with Matchers with BeforeAndAfterAll with LambdaApplicationConfigurationIT {

  behavior of s"${this.getClass.getSimpleName}"

  it should "Create Function Request" in {

    // g i v e n
    val HANDLER               = s"${this.getClass.getPackageName}.SampleLambda"
    val _                     = "sbt aws/assembly".!
    val sdkBytes              = SdkBytes.fromInputStream(new FileInputStream(s"${System.getProperty("user.dir")}/modules/aws/target/scala-2.13/aws-0.1.0.jar"))
    val functionCode          = FunctionCode.builder().zipFile(sdkBytes).build()
    val createFunctionRequest = CreateFunctionRequest
      .builder()
      .code(functionCode)
      .description(DESCRIPTION)
      .functionName(FUNCTION_NAME)
      .handler(HANDLER)
      .layers()
      .memorySize(128)
      .role(ROLE_ARN)
      .runtime(Runtime.JAVA11)
      .tags(TAGS)
      .timeout(600)
      .build()

    // w h e n
    val createFunctionResponse: Future[CreateFunctionResponse] = lambdaAsyncClientDefault
      .createFunction(createFunctionRequest)
      .toScala

    // t h e n
    createFunctionResponse.map { actual =>
      actual.sdkHttpResponse().statusCode() shouldBe HttpStatusCode.OK
      actual.description() shouldBe DESCRIPTION
      actual.functionArn() shouldBe FUNCTION_ARN
      actual.functionName() shouldBe FUNCTION_NAME
      actual.handler() shouldBe "io.github.mvillafuertem.aws.lambda.SampleLambda"
    }

  }

  it should "List Functions Request" in {

    // g i v e n
    val listFunctionsRequest = ListFunctionsRequest
      .builder()
      .maxItems(2)
      .build()

    // w h e n
    val listFunctionsResponse: Future[ListFunctionsResponse] = lambdaAsyncClientDefault
      .listFunctions(listFunctionsRequest)
      .toScala

    // t h e n
    listFunctionsResponse.map { actual =>
      actual.functions() should have size 1
      actual.sdkHttpResponse().statusCode() shouldBe HttpStatusCode.OK
    }

  }

  it should "Invoke Request" in {

    // g i v e n
    val weatherData   = WeatherData(8, 8, 8.0, 9)
    val sdkBytes      = SdkBytes.fromUtf8String(weatherData.asJson.noSpaces)
    val invokeRequest = InvokeRequest
      .builder()
      .functionName(FUNCTION_NAME)
      .payload(sdkBytes)
      .invocationType(InvocationType.REQUEST_RESPONSE)
      .build()

    // w h e n
    val invokeResponse: Future[InvokeResponse] = lambdaAsyncClientDefault
      .invoke(invokeRequest)
      .toScala

    // t h e n
    invokeResponse.map { actual =>
      actual.sdkHttpResponse().statusCode() shouldBe HttpStatusCode.OK
      decode[WeatherData](actual.payload().asUtf8String()) shouldBe Right(weatherData)
    }

  }

  override protected def beforeAll(): Unit = dockerInfrastructure.start()

  override protected def afterAll(): Unit = dockerInfrastructure.stop()

}

object LambdaApplicationIT {

  trait LambdaApplicationConfigurationIT {

    private val LAMBDA_HOST: String    = "http://localhost"
    private val LAMBDA_PORT: Int       = 4566
    val DESCRIPTION: String            = "A Scala function"
    val FUNCTION_NAME: String          = "function-test"
    val FUNCTION_ARN: String           = s"arn:aws:lambda:us-east-1:000000000000:function:$FUNCTION_NAME"
    val KEY: String                    = "logback-test.xml"
    val LAMBDA_ENDPOINT: String        = s"$LAMBDA_HOST:$LAMBDA_PORT"
    val ROLE_ARN: String               = "arn:aws:iam::000000000000:role/integration-test"
    val TAGS: util.Map[String, String] = Map("environment" -> "it").asJava

    val dockerInfrastructure: containers.DockerComposeContainer[_] =
      DockerComposeContainer(
        new File(s"${System.getProperty("user.dir")}/modules/aws/src/it/resources/docker-compose.it.yml"),
        exposedServices = Seq(ExposedService("localstack", LAMBDA_PORT, 1, Wait.forLogMessage(".*Starting mock Lambda service.*\\n", 1))),
        identifier = "docker_infrastructure"
      ).container

    val lambdaAsyncClientDefault: LambdaAsyncClient = lambdaAsyncClient(
      region = Some(Region.US_EAST_1),
      endpoint = Some(URI.create(LAMBDA_ENDPOINT)),
      credentialsProvider = Some(
        StaticCredentialsProvider.create(
          AwsBasicCredentials.create(
            "accessKey",
            "secretKey"
          )
        )
      )
    )

    def lambdaAsyncClient(
      region: Option[Region] = None,
      endpoint: Option[URI] = None,
      credentialsProvider: Option[AwsCredentialsProvider] = None
    ): LambdaAsyncClient =
      LambdaAsyncClient
        .builder()
        .add(region, _.region)
        .add(endpoint, _.endpointOverride)
        .add(credentialsProvider, _.credentialsProvider)
        .build()

  }

}
