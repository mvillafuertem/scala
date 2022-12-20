package io.github.mvillafuertem.terraform.cdktf

import org.scalablytyped.runtime.StringDictionary
import typings.cdktf.libTerraformDependableMod.ITerraformDependable
import typings.cdktf.libTerraformStackMod.TerraformStack
import typings.cdktfProviderAws.libBudgetsBudgetMod.{ BudgetsBudget, BudgetsBudgetConfig, BudgetsBudgetCostTypes, BudgetsBudgetNotification }
import typings.cdktfProviderAws.libDataAwsRegionMod.DataAwsRegion
import typings.cdktfProviderAws.libIamGroupMod.IamGroupConfig
import typings.cdktfProviderAws.libIamGroupPolicyAttachmentMod.{ IamGroupPolicyAttachment, IamGroupPolicyAttachmentConfig }
import typings.cdktfProviderAws.libIamUserMod.{ IamUser, IamUserConfig }
import typings.cdktfProviderAws.libInstanceMod.{ Instance, InstanceConfig, InstanceEbsBlockDevice }
import typings.cdktfProviderAws.libProviderMod.{ AwsProvider, AwsProviderConfig }
import typings.cdktfProviderAws.libS3BucketMod.{ S3BucketConfig, S3BucketVersioning }
import typings.cdktfProviderAws.libSecurityGroupMod.SecurityGroupConfig
import typings.cdktfProviderAws.libSecurityGroupRuleMod.SecurityGroupRuleConfig
import typings.cdktfProviderAws.mod.iamGroup.IamGroup
import typings.cdktfProviderAws.mod.s3Bucket.S3Bucket
import typings.cdktfProviderAws.mod.securityGroup.SecurityGroup
import typings.cdktfProviderAws.mod.securityGroupRule.SecurityGroupRule
import typings.constructs.mod.Construct

import scala.scalajs.js

final class IamStack(scope: Construct, name: String) extends TerraformStack(scope, name) { self: Construct =>

  private val groupName = "developers"
  private val groupId   = groupName
  private val path      = "/users/"
  private val userName  = "pepe"
  private val userId    = userName
  private val region    = "us-east-1"

  new AwsProvider(self, "aws", AwsProviderConfig().setRegion(region))

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
    BudgetsBudgetConfig("COST", "1.0")
      .setLimitUnit("USD")
      .setTimeUnit("MONTHLY")
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
      .setDependsOn(js.Array[ITerraformDependable](group.asInstanceOf[ITerraformDependable]))
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
      .setSecurityGroups(js.Array[String](securityGroup.name))
      .setEbsBlockDevice(js.Array[InstanceEbsBlockDevice](ebsBlockDevice))
    // .deleteRootBlockDevice
  )

}
