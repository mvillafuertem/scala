package io.github.mvillafuertem

import software.amazon.awssdk.services.lambda.LambdaAsyncClientBuilder
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder

package object aws {

  implicit class RichLambdaAsyncClientBuilder(lambdaClientBuilder: LambdaAsyncClientBuilder) {
    def add[T](value: Option[T], builder: LambdaAsyncClientBuilder => T => LambdaAsyncClientBuilder): LambdaAsyncClientBuilder =
      value.fold(lambdaClientBuilder)(builder(lambdaClientBuilder))
  }
  implicit class RichS3AsyncClientBuilder(s3ClientBuilder: S3AsyncClientBuilder)             {
    def add[T](value: Option[T], builder: S3AsyncClientBuilder => T => S3AsyncClientBuilder): S3AsyncClientBuilder =
      value.fold(s3ClientBuilder)(builder(s3ClientBuilder))
  }

}
