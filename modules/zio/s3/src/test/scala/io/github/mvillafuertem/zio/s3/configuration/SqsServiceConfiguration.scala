package io.github.mvillafuertem.zio.s3.configuration

import software.amazon.awssdk.auth.credentials.{ AwsBasicCredentials, StaticCredentialsProvider }
import zio.ZLayer
import zio.aws.core.config.CommonAwsConfig
import zio.aws.sqs.Sqs

import java.net.URI

trait SqsServiceConfiguration { _: LocalstackConfiguration =>

  protected val sqsLayer: ZLayer[Any, Throwable, Sqs] =
    zio.aws.netty.NettyHttpClient.default ++
      ZLayer.succeed(
        CommonAwsConfig(
          region = Some(defaultRegion),
          credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretAccessKey)),
          endpointOverride = Some(URI.create(uri)),
          commonClientConfig = None
        )
      ) >>>
      zio.aws.core.config.AwsConfig.configured() >>>
      zio.aws.sqs.Sqs.live

}
