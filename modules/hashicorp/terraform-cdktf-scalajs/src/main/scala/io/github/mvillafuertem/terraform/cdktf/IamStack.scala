package io.github.mvillafuertem.terraform.cdktf

import org.scalablytyped.runtime.StringDictionary
import typings.cdktf.cdktfMod.TerraformStack
import typings.cdktf.terraformDependableMod.ITerraformDependable
import typings.cdktfProviderAws.awsProviderMod.AwsProviderConfig
import typings.cdktfProviderAws.budgetsMod.Budgets.{ BudgetsBudget, BudgetsBudgetConfig, BudgetsBudgetCostTypes, BudgetsBudgetNotification }
import typings.cdktfProviderAws.ec2Mod.EC2.{ Instance, InstanceConfig, InstanceEbsBlockDevice }
import typings.cdktfProviderAws.iamMod.IAM._
import typings.cdktfProviderAws.mod.DataSources.DataAwsRegion
import typings.cdktfProviderAws.mod.IAM.IamGroup
import typings.cdktfProviderAws.mod.S3.S3Bucket
import typings.cdktfProviderAws.mod.VPC.{ SecurityGroup, SecurityGroupRule }
import typings.cdktfProviderAws.mod._
import typings.cdktfProviderAws.s3Mod.S3.{ S3BucketConfig, S3BucketVersioning }
import typings.cdktfProviderAws.vpcMod.VPC.{ SecurityGroupConfig, SecurityGroupRuleConfig }
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

  new DataAwsRegion(self, region)

  val s3 = new S3Bucket(
    self,
    "s3",
    S3BucketConfig()
      .setBucket("cdktf")
      .setVersioning(
        S3BucketVersioning()
          .setEnabled(true)
      )
  )

  // BudgetsBudgetConfig

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
    BudgetsBudgetConfig("COST", "1.0", "USD", "MONTHLY")
      .setName("Monthly Cost Budget")
      .setTimePeriodStart("2020-11-06_00:00")
      .setCostTypes(
        BudgetsBudgetCostTypes()
          .setIncludeRefund(true)
          .setIncludeCredit(true)
          .setIncludeUpfront(true)
          .setIncludeRecurring(true)
          .setIncludeOtherSubscription(true)
          .setIncludeTax(true)
          .setIncludeSupport(true)
      )
      .setNotification(
        js.Array[BudgetsBudgetNotification](
          BudgetsBudgetNotification("GREATER_THAN", "FORECASTED", 10, "PERCENTAGE")
            .setSubscriberEmailAddresses(js.Array[String]("pepe@gmail.com"))
        )
      )
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
      .setDependsOn(js.Array[ITerraformDependable](group))
  )

  val user = new IamUser(
    self,
    userId,
    IamUserConfig(userName).setPath(path)
  )

  private val securityGroup = new SecurityGroup(
    self,
    "securityGroup",
    SecurityGroupConfig()
      .setName("securityGroup")
      .setDescription("securityGroup")
      .setVpcId("")
      .setTags(StringDictionary("Name" -> ""))
  )

  new SecurityGroupRule(
    self,
    "securityGroupRuleIngress",
    SecurityGroupRuleConfig(0, "-1", securityGroup.id, 0, "ingress")
      .setCidrBlocks(js.Array[String]("0.0.0.0/0"))
  )

  new SecurityGroupRule(
    self,
    "securityGroupRuleEgress",
    SecurityGroupRuleConfig(0, "all", securityGroup.id, 0, "egress")
      .setCidrBlocks(js.Array[String]("0.0.0.0/0"))
  )

  private val ebsBlockDevice: InstanceEbsBlockDevice = InstanceEbsBlockDevice("/dev/xvda")
    .setVolumeSize(8)
    .setVolumeType("gp2")
    .setIops(100)
    .setEncrypted(false)

  new Instance(
    self,
    "instance",
    InstanceConfig()
      .setAmi("ami-0947d2ba12ee1ff75")
      .setInstanceType("t2.micro")
      .setTags(StringDictionary("Name" -> "instance"))
      .setSecurityGroups(js.Array[String](securityGroup.name.get))
      .setEbsBlockDevice(js.Array[InstanceEbsBlockDevice](ebsBlockDevice))
    // .deleteRootBlockDevice
  )

}
