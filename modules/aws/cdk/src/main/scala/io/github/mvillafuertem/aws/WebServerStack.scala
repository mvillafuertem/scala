package io.github.mvillafuertem.aws

import java.util

import software.amazon.awscdk.core.{ Construct, Stack }
import software.amazon.awscdk.services.ec2.{ SubnetConfiguration, SubnetType, Vpc, VpcProps }
import software.amazon.awscdk.services.ecs.patterns.{
  ApplicationLoadBalancedFargateService,
  ApplicationLoadBalancedFargateServiceProps,
  ApplicationLoadBalancedTaskImageOptions
}
import software.amazon.awscdk.services.ecs.{ Cluster, ClusterProps, ContainerImage }

class WebServerStack(scope: Construct, id: String) extends Stack(scope, id) {

  val vpc = new Vpc(
    this,
    "Vpc",
    VpcProps
      .builder()
      .maxAzs(2)
      .subnetConfiguration(
        util.Arrays.asList(
          SubnetConfiguration.builder().name("PublicSubnet").subnetType(SubnetType.PUBLIC).build()
        )
      )
      .build()
  )

  val cluster = new Cluster(this, "Cluster", ClusterProps.builder().vpc(vpc).build())

  val fargate: ApplicationLoadBalancedFargateService = {
    val imageOpts = ApplicationLoadBalancedTaskImageOptions
      .builder()
      .image(ContainerImage.fromAsset("modules/cask/target/docker/stage/"))
      .containerPort(8080)
      .build()

    new ApplicationLoadBalancedFargateService(
      this,
      "Fargate",
      ApplicationLoadBalancedFargateServiceProps
        .builder()
        .cluster(cluster)
        .assignPublicIp(true)
        .cpu(256)
        .memoryLimitMiB(512)
        .taskImageOptions(imageOpts)
        .build()
    )
  }

}
