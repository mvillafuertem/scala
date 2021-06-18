package io.github.mvillafuertem.terraform.cdktf

import com.hashicorp.cdktf.TerraformStack
import imports.aws._
import software.constructs.Construct

import scala.jdk.CollectionConverters._

final class CdktfStack(scope: Construct) extends TerraformStack(scope, "cdktf-terraform-stack") {
  self: Construct =>

  private val _: AwsProvider = AwsProvider.Builder
    .create(self, "cdktf_aws_provider")
    .region("eu-west-1")
    .build()

  private val vpc: DataAwsVpc = DataAwsVpc.Builder
    .create(self, "cdktf_vpc")
    .build()

  private val keyPair: KeyPair = KeyPair.Builder
    .create(self, "cdktf_key_pair")
    .keyName("cdktf-key-pair")
    .publicKey("id_rsa.pub")
    .build()

  private val securityGroup: DataAwsSecurityGroup = DataAwsSecurityGroup.Builder
    .create(self, "cdktf_security_group")
    .name("cdktf-security-group")
    .vpcId(vpc.getId)
    .build()

  private val _: SecurityGroupRule = SecurityGroupRule.Builder
    .create(self, "cdktf_ingress")
    .fromPort(22)
    .toPort(22)
    .protocol("tcp")
    .`type`("ingress")
    .securityGroupId(securityGroup.getId)
    .cidrBlocks(List[String]("0.0.0.0/0").asJava)
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

  private val _: Instance = Instance.Builder
    .create(self, "cdktf_instance")
    .ami("ami-0947d2ba12ee1ff75")
    .keyName(keyPair.getKeyName)
    .instanceType("t2.micro")
    .securityGroups(List(securityGroup.getName).asJava)
    .associatePublicIpAddress(true)
    .build()

}
