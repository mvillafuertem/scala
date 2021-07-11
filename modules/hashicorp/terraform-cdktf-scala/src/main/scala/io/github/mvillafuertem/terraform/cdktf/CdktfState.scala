package io.github.mvillafuertem.terraform.cdktf

import com.hashicorp.cdktf.{ TerraformOutput, TerraformStack }
import imports.aws._
import io.github.mvillafuertem.terraform.cdktf.CdktfStack.CdktfStackConfiguration
import software.constructs.Construct

import java.util
import scala.jdk.CollectionConverters._

final class CdktfState(scope: Construct, id: String, cdktfStackConfiguration: CdktfStackConfiguration) extends TerraformStack(scope, id) {
  self: Construct =>

  private val _: AwsProvider = AwsProvider.Builder
    .create(self, "cdktf_aws_provider")
    .allowedAccountIds(List(cdktfStackConfiguration.accountId).asJava)
    .region(cdktfStackConfiguration.region)
    .sharedCredentialsFile(cdktfStackConfiguration.sharedCredentialsFile)
    .profile(cdktfStackConfiguration.profile)
    .build()

  private val serverSideEncryptionConfiguration: util.List[S3BucketServerSideEncryptionConfiguration] = List(
    S3BucketServerSideEncryptionConfiguration
      .builder()
      .rule(
        List(
          S3BucketServerSideEncryptionConfigurationRule
            .builder()
            .applyServerSideEncryptionByDefault(
              List(
                S3BucketServerSideEncryptionConfigurationRuleApplyServerSideEncryptionByDefault
                  .builder()
                  .sseAlgorithm("AES256")
                  .build()
              ).asJava
            )
            .build()
        ).asJava
      )
      .build()
  ).asJava

  private val versioning: util.List[S3BucketVersioning] = List(
    S3BucketVersioning
      .builder()
      .enabled(true)
      .build()
  ).asJava

  private val s3Bucket: S3Bucket = S3Bucket.Builder
    .create(self, "cdktf_s3")
    .bucket(cdktfStackConfiguration.bucket)
    .versioning(versioning)
    .forceDestroy(true)
    .serverSideEncryptionConfiguration(serverSideEncryptionConfiguration)
    .tags(Map("Environment" -> cdktfStackConfiguration.environment).asJava)
    .build()

  private val _: DynamodbTable = DynamodbTable.Builder
    .create(self, "cdktf_dynamodb")
    .name(cdktfStackConfiguration.dynamodbTable)
    .billingMode("PAY_PER_REQUEST")
    .hashKey("LockID")
    .attribute(List(DynamodbTableAttribute.builder().name("LockID").`type`("S").build()).asJava)
    .tags(Map("Environment" -> cdktfStackConfiguration.environment).asJava)
    .build()

  private val _: TerraformOutput = TerraformOutput.Builder
    .create(self, "cdktf_tfstate")
    .value(s"${s3Bucket.getArn}")
    .build()

}
