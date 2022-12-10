package io.github.mvillafuertem.terraform.cdktf

import com.hashicorp.cdktf.{ TerraformOutput, TerraformResourceLifecycle }
import imports.aws.autoscaling.AutoscalingGroup
import imports.aws.datasources.LaunchConfiguration
import imports.aws.ec2.KeyPair
import imports.aws.elb._
import imports.aws.vpc.{ DataAwsVpc, SecurityGroup, SecurityGroupRule, Subnet }
import io.github.mvillafuertem.terraform.cdktf.CdktfStack.CdktfStackConfiguration
import software.constructs.Construct

import scala.jdk.CollectionConverters._

final class CdktfBastion(scope: Construct, cdktfStackConfiguration: CdktfStackConfiguration, keyPair: KeyPair, vpc: DataAwsVpc) {

  private val subnet: Subnet = Subnet.Builder
    .create(scope, "cdktf_subnet")
    .vpcId(vpc.getId)
    .cidrBlock("10.0.0.0/24")
    .tags(Map("Environment" -> cdktfStackConfiguration.environment).asJava)
    .build()

  private val securityGroup: SecurityGroup = SecurityGroup.Builder
    .create(scope, "cdktf_bastion_security_group")
    .name("cdktf-bastion-security-group")
    .vpcId(vpc.getId)
    .tags(Map("Environment" -> cdktfStackConfiguration.environment).asJava)
    .build()

  private val alb: Alb = Alb.Builder
    .create(scope, "cdktf_bastion_alb")
    .name("cdktf-bastion-alb")
    .securityGroups(List(securityGroup.getId).asJava)
    .subnets(List(subnet.getId).asJava)
    .tags(Map("Environment" -> cdktfStackConfiguration.environment).asJava)
    .build()

  private val albTargetGroup: AlbTargetGroup = AlbTargetGroup.Builder
    .create(scope, "cdktf_bastion_alb_target_group")
    .port(80)
    .protocol("HTTP")
    .vpcId(vpc.getId)
    .healthCheck(
      AlbTargetGroupHealthCheck
        .builder()
        .path("/health")
        .port("80")
        .build()
    )
    .build()

  private val _: AlbListener = AlbListener.Builder
    .create(scope, "cdktf_bastion_alb_listener")
    .loadBalancerArn(alb.getArn)
    .port(80)
    .protocol("HTTP")
    .defaultAction(
      List(
        AlbListenerDefaultAction
          .builder()
          .targetGroupArn(albTargetGroup.getArn)
          .`type`("forward")
          .build()
      ).asJava
    )
    .build()

  private val _: SecurityGroupRule = SecurityGroupRule.Builder
    .create(scope, "cdktf_bastion_ingress")
    .fromPort(22)
    .toPort(22)
    .protocol("tcp")
    .`type`("ingress")
    .securityGroupId(securityGroup.getId)
    .cidrBlocks(List[String]("0.0.0.0/0").asJava)
    .description("VPN Madrid")
    .build()

  private val _: SecurityGroupRule = SecurityGroupRule.Builder
    .create(scope, "cdktf_bastion_egress")
    .fromPort(0)
    .toPort(0)
    .protocol("-1")
    .`type`("egress")
    .securityGroupId(securityGroup.getId)
    .cidrBlocks(List[String]("0.0.0.0/0").asJava)
    .build()

//  private val bastionInstance: Instance = Instance.Builder
//    .create(scope, "cdktf_bastion_instance")
//    .ami("ami-0f89681a05a3a9de7")
//    .keyName(keyPair.getKeyName)
//    .instanceType("t2.micro")
//    .securityGroups(List(securityGroup.getName).asJava)
//    .associatePublicIpAddress(true)
//    .userData(cdktfStackConfiguration.userData)
//    .tags(
//      Map(
//        "Name"        -> "cdktf-bastion-instance",
//        "Environment" -> cdktfStackConfiguration.environment
//      ).asJava
//    )
//    .build()

  private val launchConfiguration: LaunchConfiguration = LaunchConfiguration.Builder
    .create(scope, "cdktf_bastion_launch_configuration")
    .namePrefix("cdktf-bastion-instance")
    .imageId("ami-0f89681a05a3a9de7")
    .associatePublicIpAddress(true)
    .instanceType("t2.micro")
    .keyName(keyPair.getKeyName)
    .userData(cdktfStackConfiguration.userData)
    .securityGroups(List(securityGroup.getId).asJava)
    .lifecycle(TerraformResourceLifecycle.builder().createBeforeDestroy(true).build())
    .build()

  private val _: AutoscalingGroup = AutoscalingGroup.Builder
    .create(scope, "cdktf_bastion_autoscaling_group")
    .minSize(0)
    .maxSize(1)
    .launchConfiguration(launchConfiguration.getId)
    .targetGroupArns(List(albTargetGroup.getArn).asJava)
    .vpcZoneIdentifier(List().asJava)
    .build()

  private val _: TerraformOutput = TerraformOutput.Builder
    .create(scope, "cdktf_bastion_ssh_copy_id")
    .value(s"ssh-copy-id ec2-user@${alb.getDnsName}")
    .build()

  private val _: TerraformOutput = TerraformOutput.Builder
    .create(scope, "cdktf_bastion_ssh_config")
    .value(
      s"""
         |vi ~/.ssh/config
         |
         |Host bastion
         |    Hostname ${alb.getDnsName}
         |    User ec2-user
         |""".stripMargin)
    .build()

  private val _: TerraformOutput = TerraformOutput.Builder
    .create(scope, "cdktf_bastion_ssh_connection")
    .value("ssh bastion")
    .build()

}
