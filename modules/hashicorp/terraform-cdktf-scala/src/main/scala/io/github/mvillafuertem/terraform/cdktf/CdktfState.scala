package io.github.mvillafuertem.terraform.cdktf

import com.hashicorp.cdktf.{ TerraformOutput, TerraformStack }
import imports.aws._
import imports.aws.dynamo_db.{ DynamodbTable, DynamodbTableAttribute }
import imports.aws.s3._
import io.github.mvillafuertem.terraform.cdktf.CdktfStack.CdktfStackConfiguration
import software.constructs.Construct

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

  private val serverSideEncryptionConfiguration: S3BucketServerSideEncryptionConfiguration =
    S3BucketServerSideEncryptionConfiguration
      .builder()
      .rule(
        S3BucketServerSideEncryptionConfigurationRule
          .builder()
          .applyServerSideEncryptionByDefault(
            S3BucketServerSideEncryptionConfigurationRuleApplyServerSideEncryptionByDefault
              .builder()
              .sseAlgorithm("AES256")
              .build()
          )
          .build()
      )
      .build()

  private val versioning: S3BucketVersioning =
    S3BucketVersioning
      .builder()
      .enabled(true)
      .build()

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
