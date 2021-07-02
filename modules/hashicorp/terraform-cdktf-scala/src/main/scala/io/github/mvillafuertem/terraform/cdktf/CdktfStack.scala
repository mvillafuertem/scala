package io.github.mvillafuertem.terraform.cdktf

import com.hashicorp.cdktf.{TerraformOutput, TerraformStack}
import imports.aws._
import io.github.mvillafuertem.terraform.cdktf.CdktfStack.CdktfStackConfiguration
import software.constructs.Construct

import scala.jdk.CollectionConverters._

final class CdktfStack(scope: Construct, id: String, cdktfStackConfiguration: CdktfStackConfiguration) extends TerraformStack(scope, id) {
  self: Construct =>

  private val _: AwsProvider = AwsProvider.Builder
    .create(self, "cdktf_aws_provider")
    .allowedAccountIds(List(cdktfStackConfiguration.accountId).asJava)
    .region(cdktfStackConfiguration.region)
    .sharedCredentialsFile(cdktfStackConfiguration.sharedCredentialsFile)
    .profile(cdktfStackConfiguration.profile)
    .build()

  private val vpc: DataAwsVpc = DataAwsVpc.Builder
    .create(self, "cdktf_vpc")
    .defaultValue(true)
    .build()

  private val keyPair: KeyPair = KeyPair.Builder
    .create(self, "cdktf_key_pair")
    .keyName("cdktf-key")
    .publicKey(cdktfStackConfiguration.publicKey)
    .build()

  private val securityGroup: SecurityGroup = SecurityGroup.Builder
    .create(self, "cdktf_security_group")
    .name("cdktf-security-group")
    .vpcId(vpc.getId)
    .tags(Map("Environment" -> cdktfStackConfiguration.environment).asJava)
    .build()

  private val _: SecurityGroupRule = SecurityGroupRule.Builder
    .create(self, "cdktf_ingress_tcp")
    .fromPort(22)
    .toPort(22)
    .protocol("tcp")
    .`type`("ingress")
    .securityGroupId(securityGroup.getId)
    .cidrBlocks(List[String]("0.0.0.0/0").asJava)
    .description("VPN Madrid")
    .build()

  private val _: SecurityGroupRule = SecurityGroupRule.Builder
    .create(self, "cdktf_ingress_http")
    .fromPort(80)
    .toPort(80)
    .protocol("tcp")
    .`type`("ingress")
    .securityGroupId(securityGroup.getId)
    .cidrBlocks(List[String]("0.0.0.0/0").asJava)
    .description("Jira")
    .build()

  private val _: SecurityGroupRule = SecurityGroupRule.Builder
    .create(self, "cdktf_egress")
    .fromPort(0)
    .toPort(0)
    .protocol("-1")
    .`type`("egress")
    .securityGroupId(securityGroup.getId)
    .cidrBlocks(List[String]("0.0.0.0/0").asJava)
    .build()

  private val _ = new CdktfBastion(self, cdktfStackConfiguration, keyPair, vpc)
  private val instance: Instance = Instance.Builder
    .create(self, "cdktf_instance")
    .ami("ami-0f89681a05a3a9de7")
    .keyName(keyPair.getKeyName)
    .instanceType("t2.medium")
    //.securityGroups(List(securityGroup.getName).asJava)
    .associatePublicIpAddress(true)
    .userData(cdktfStackConfiguration.userData)
    .tags(
      Map(
        "Name"        -> "cdktf-instance",
        "Environment" -> cdktfStackConfiguration.environment
      ).asJava
    )
    .build()

  private val _: NetworkInterfaceSgAttachment = NetworkInterfaceSgAttachment.Builder
    .create(self, "cdktf_network_interface_sg_attachment")
    .securityGroupId(securityGroup.getId)
    .networkInterfaceId(instance.getPrimaryNetworkInterfaceId)
    .build()

  private val _: BudgetsBudget = BudgetsBudget.Builder
    .create(self, "cdktf_budgets_budget")
    .name("Monthly Cost Budget")
    .limitUnit("USD")
    .limitAmount("1.0")
    .budgetType("COST")
    .timePeriodStart("2021-06-01_00:00")
    .timeUnit("MONTHLY")
    .costTypes(
      List(
        BudgetsBudgetCostTypes
          .builder()
          .includeRefund(true)
          .includeCredit(true)
          .includeUpfront(true)
          .includeRecurring(true)
          .includeOtherSubscription(true)
          .includeTax(true)
          .includeSupport(true)
          .build()
      ).asJava
    )
    .notification(
      List(
        BudgetsBudgetNotification
          .builder()
          .comparisonOperator("GREATER_THAN")
          .notificationType("FORECASTED")
          .threshold(10)
          .thresholdType("PERCENTAGE")
          .subscriberEmailAddresses(List("pepe@gmail.com").asJava)
          .subscriberSnsTopicArns(List().asJava)
          .build()
      ).asJava
    )
    .build()

  private val _: TerraformOutput = TerraformOutput.Builder
    .create(self, "instance_ip")
    .value(instance.getPublicIp)
    .build()

  private val _: TerraformOutput = TerraformOutput.Builder
    .create(self, "instance_dns")
    .value(instance.getPublicDns)
    .build()

  private val _: TerraformOutput = TerraformOutput.Builder
    .create(self, "ping_connection")
    .value(s"ping ${instance.getPublicIp}")
    .build()

  private val _: TerraformOutput = TerraformOutput.Builder
    .create(self, "ssh_connection")
    .value(s"ssh -i modules/hashicorp/terraform-cdktf-scala/src/main/resources/ssh/id_rsa ec2-user@${instance.getPublicDns}")
    .build()

}

object CdktfStack {
  def apply(scope: Construct, id: String, cdktfStackConfiguration: CdktfStackConfiguration): CdktfStack =
    new CdktfStack(scope, id, cdktfStackConfiguration)

  case class CdktfStackConfiguration(
    accessKey: String,
    accountId: String,
    bucket: String,
    dynamodbTable: String,
    environment: String,
    profile: String,
    publicKey: String,
    region: String,
    secretKey: String,
    sharedCredentialsFile: String,
    userData: String
  )
}
