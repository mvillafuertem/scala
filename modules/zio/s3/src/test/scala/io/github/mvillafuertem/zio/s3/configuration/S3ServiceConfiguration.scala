package io.github.mvillafuertem.zio.s3.configuration

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import zio.ZLayer
import zio.s3.S3
import zio.test.TestFailure

import java.net.URI

trait S3ServiceConfiguration { _: LocalstackConfiguration =>

  protected val bucketName = "bucketname"

  protected val s3Layer: ZLayer[Any, TestFailure[Nothing], S3] =
    zio.s3
      .live(
        defaultRegion,
        AwsBasicCredentials.create(accessKey, secretAccessKey),
        Some(URI.create(uri))
      )
      .mapError(TestFailure.die)

}
