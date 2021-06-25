package io.github.mvillafuertem.terraform.cdktf

import com.hashicorp.cdktf.{TerraformOutput, TerraformStack}
import imports.aws._
import software.constructs.Construct

import scala.io.Source
import scala.jdk.CollectionConverters._

final class CdktfStack(scope: Construct) extends TerraformStack(scope, "cdktf-terraform-stack") {
  self: Construct =>

  private val accessKey = Source
    .fromInputStream(getClass.getResourceAsStream("/aws.credentials/access_key"))
    .getLines()
    .mkString("\n")

  private val secretKey = Source
    .fromInputStream(getClass.getResourceAsStream("/aws.credentials/secret_key"))
    .getLines()
    .mkString("\n")

  private val _: AwsProvider = AwsProvider.Builder
    .create(self, "cdktf_aws_provider")
    .allowedAccountIds(List("").asJava)
    .region("eu-west-1")
    //.accessKey(accessKey)
    //.secretKey(secretKey)
    .build()

  private val vpc: DataAwsVpc = DataAwsVpc.Builder
    .create(self, "cdktf_vpc")
    .defaultValue(true)
    .build()

  private val publicKey = Source
    .fromInputStream(getClass.getResourceAsStream("/ssh/id_rsa.pub"))
    .getLines()
    .mkString("\n")

  private val keyPair: KeyPair = KeyPair.Builder
    .create(self, "cdktf_key_pair")
    .keyName("cdktf-key")
    .publicKey(publicKey)
    .build()

  private val securityGroup: SecurityGroup = SecurityGroup.Builder
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
    .description("VPN Madrid")
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

  private val userData = Source
    .fromInputStream(getClass.getResourceAsStream("/userData.yml"))
    .getLines()
    .mkString("\n")

  private val instance: Instance = Instance.Builder
    .create(self, "cdktf_instance")
    .ami("ami-0f89681a05a3a9de7")
    .keyName(keyPair.getKeyName)
    .instanceType("t2.micro")
    //.securityGroups(List(securityGroup.getName).asJava)
    //.associatePublicIpAddress(true)
    .userData(userData)
    .tags(Map("Name" -> "cdktf_instance").asJava)
    .build()

  private val _: NetworkInterfaceSgAttachment = NetworkInterfaceSgAttachment.Builder
    .create(self, "cdktf_network_interface_sg_attachment")
    .securityGroupId(securityGroup.getId)
    .networkInterfaceId(instance.getPrimaryNetworkInterfaceId)
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
    .create(self, "ssh_connection")
    .value(s"ssh -i modules/hashicorp/terraform-cdktf-scala/src/main/resources/ssh/id_rsa ec2-user@${instance.getPublicDns}")
    .build()
}
