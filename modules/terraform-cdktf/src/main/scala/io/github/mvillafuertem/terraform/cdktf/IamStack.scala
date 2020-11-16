package io.github.mvillafuertem.terraform.cdktf

import typings.cdktf.mod.{ S3Backend, TerraformStack }
import typings.cdktf.s3BackendMod.S3BackendProps
import typings.cdktf.terraformResourceMod.TerraformResource
import typings.cdktfProviderAws.awsProviderMod.AwsProviderConfig
import typings.cdktfProviderAws.budgetsBudgetMod.{ BudgetsBudgetConfig, BudgetsBudgetCostTypes, BudgetsBudgetNotification }
import typings.cdktfProviderAws.cloudwatchMetricAlarmMod.CloudwatchMetricAlarmConfig
import typings.cdktfProviderAws.iamGroupMod.IamGroupConfig
import typings.cdktfProviderAws.iamGroupPolicyAttachmentMod.IamGroupPolicyAttachmentConfig
import typings.cdktfProviderAws.iamUserMod.IamUserConfig
import typings.cdktfProviderAws.instanceMod.{ InstanceConfig, InstanceEbsBlockDevice }
import typings.cdktfProviderAws.mod.Instance
import typings.cdktfProviderAws.mod._
import typings.cdktfProviderAws.mod.SnsTopicSubscription
import typings.cdktfProviderAws.s3BucketMod.{ S3BucketConfig, S3BucketVersioning }
import typings.cdktfProviderAws.securityGroupMod.{ SecurityGroupConfig, SecurityGroupEgress, SecurityGroupIngress }
import typings.cdktfProviderAws.snsTopicMod.SnsTopicConfig
import typings.cdktfProviderAws.snsTopicSubscriptionMod.SnsTopicSubscriptionConfig
import typings.constructs.mod.Construct

import scala.scalajs.js

final class IamStack(scope: Construct, name: String) extends TerraformStack(scope, name) { self: Construct =>

  private val groupName = "developers"
  private val groupId   = groupName
  private val path      = "/users/"
  private val userName  = "pepe"
  private val userId    = userName
  private val region    = "us-east-1"

  new AwsProvider(self, "aws", AwsProviderConfig(region))

  val s3 = new S3Bucket(
    self,
    "s3",
    S3BucketConfig()
      .setBucket("cdktf")
      .setVersioning(
        js.Array[S3BucketVersioning](
          S3BucketVersioning()
            .setEnabled(true)
        )
      )
      .setRegion(region)
  )

  //BudgetsBudgetConfig

//  private val topic: SnsTopic = new SnsTopic(
//    self,
//    "billing-cloudwatch-alarms-topic",
//    SnsTopicConfig()
//      .setName("Billing_Cloudwatch_Alarms_Topic")
//  )
//
//  new SnsTopicSubscription(
//    self,
//    "billing-cloudwatch-alarms-subscription",
//    SnsTopicSubscriptionConfig("pepe@gmail.com", "email", topic.arn)
//      .setDependsOn(js.Array[TerraformResource](topic))
//  )

//  new CloudwatchMetricAlarm(
//    self,
//    "billing-alarm",
//    CloudwatchMetricAlarmConfig("Billing Alarm", "GreaterThanThreshold", 2)
//      .setMetricName("EstimatedCharges")
//      .setThreshold(0)
//      .setDependsOn(js.Array[TerraformResource](topic))
//      .setAlarmActions(js.Array[String](topic.arn))
//      .setStatistic("Maximum")
//      .setPeriod(6)
//      .setNamespace("AWS/Billing")
//  )

  new BudgetsBudget(
    self,
    "monthly-cost-budget",
    BudgetsBudgetConfig("COST", "1.0", "USD", "2020-11-06_00:00", "MONTHLY")
      .setName("Monthly Cost Budget")
      .setCostTypes(
        js.Array[BudgetsBudgetCostTypes](
          BudgetsBudgetCostTypes()
            .setIncludeRefund(true)
            .setIncludeCredit(true)
            .setIncludeUpfront(true)
            .setIncludeRecurring(true)
            .setIncludeOtherSubscription(true)
            .setIncludeTax(true)
            .setIncludeSupport(true)
        )
      )
      .setNotification(
        js.Array[BudgetsBudgetNotification](
          BudgetsBudgetNotification("GREATER_THAN", "FORECASTED", 10, "PERCENTAGE")
            .setSubscriberEmailAddresses(js.Array[String]("pepe@gmail.com"))
        )
      )
  )

  new S3Backend(
    self,
    S3BackendProps("cdktf", "terraform.tfstate")
      .setRegion(region)
  )

  val group = new IamGroup(
    self,
    groupId,
    IamGroupConfig(groupName)
      .setPath(path)
  )

  val groupPolicyAttachment = new IamGroupPolicyAttachment(
    self,
    "groupPolicyAttachmentDevelopers",
    IamGroupPolicyAttachmentConfig(groupName, "arn:aws:iam::aws:policy/AdministratorAccess")
      .setDependsOn(js.Array[TerraformResource](group))
  )

  val user = new IamUser(
    self,
    userId,
    IamUserConfig(userName).setPath(path)
  )

  private val securityGroupIngress: SecurityGroupIngress = SecurityGroupIngress()
    .setProtocol("tcp")
    .setFromPort(22)
    .setToPort(22)
    .setCidrBlocks(js.Array[String]("0.0.0.0/0"))

  private val securityGroupEgress: SecurityGroupEgress = SecurityGroupEgress()
    .setProtocol("-1")
    .setFromPort(0)
    .setToPort(0)
    .setCidrBlocks(js.Array[String]("0.0.0.0/0"))

  private val securityGroup = new SecurityGroup(
    self,
    "security-group",
    SecurityGroupConfig()
      .setName("Security Group")
      .setIngress(js.Array[SecurityGroupIngress](securityGroupIngress))
      .setEgress(js.Array[SecurityGroupEgress](securityGroupEgress))
  )

  private val ebsBlockDevice: InstanceEbsBlockDevice = InstanceEbsBlockDevice("ebs-block-device")
    .setVolumeSize(3)
    .setVolumeType("gp2")
    .setIops(100)
    .setEncrypted(false)

  private val instance = new Instance(
    self,
    "instance",
    InstanceConfig("ami-0947d2ba12ee1ff75", "t2.micro")
      .setSecurityGroups(js.Array[String](securityGroup.name.orNull))
      .setEbsBlockDevice(js.Array[InstanceEbsBlockDevice](ebsBlockDevice))
  )

}
