package io.github.mvillafuertem.zio.s3

import io.github.mvillafuertem.zio.s3.configuration.{ LocalstackConfiguration, S3ServiceConfiguration }
import software.amazon.awssdk.services.s3.model.S3Exception
import zio.s3._
import zio.stream.{ ZPipeline, ZStream }
import zio.test.Assertion.equalTo
import zio.test.TestAspect.{ beforeAll, sequential }
import zio.test.{ assertZIO, Spec, TestEnvironment, ZIOSpecDefault }
import zio.{ Chunk, Scope, ZIO }

import scala.util.Random

object ZS3Spec extends ZIOSpecDefault with LocalstackConfiguration with S3ServiceConfiguration {

  private val keyRandom: () => String = () => Random.alphanumeric.take(10).mkString

  def stringToObject(message: String, bucketName: String, key: String): ZIO[S3 with Any, S3Exception, Unit] = {
    val bytes = message.getBytes
    val chunk = Chunk.fromArray(bytes)
    val data  = ZStream.fromChunks(chunk)
    putObject(bucketName, key, chunk.length.toLong, data)
  }

  private val test: Seq[Spec[S3, Exception]] = Seq(
    test("create bucket")(
      assertZIO(
        for {
          bucketNameTmp <- ZIO.succeed(bucketName + "tmp")
          succeed       <- createBucket(bucketNameTmp).foldCause(_ => false, _ => true).debug("bucket tmp created")
          _             <- deleteBucket(bucketNameTmp)
        } yield succeed
      )(equalTo(true))
    ),
    test("put object") {
      val c             = Chunk.fromArray(Random.nextString(65536).getBytes())
      val contentLength = c.length.toLong
      val data          = ZStream.fromChunks(c).rechunk(5)
      assertZIO(
        for {
          _                   <- createBucket(bucketName).foldCause(_ => false, _ => true).debug("bucket created")
          tmpKey              <- ZIO.succeed(keyRandom())
          _                   <- putObject(bucketName, tmpKey, contentLength, data)
          objectContentLength <- getObjectMetadata(bucketName, tmpKey).map(_.contentLength) <*
                                   deleteObject(bucketName, tmpKey)
        } yield objectContentLength
      )(equalTo(contentLength))
    },
    test("put object from stream")(
      assertZIO(for {
        _                   <- createBucket(bucketName).foldCause(_ => false, _ => true).debug("bucket created")
        tmpKey              <- ZIO.succeed(keyRandom())
        _                   <- ZStream
                                 .fromChunk(Chunk("hola"))
                                 .mapZIO(msg => stringToObject(message = msg, bucketName = bucketName, key = tmpKey))
                                 .runDrain
        objectContentLength <- getObject(bucketName, tmpKey).via(ZPipeline.utf8Decode).filter(_.nonEmpty).runCollect
      } yield objectContentLength)(equalTo(Chunk("hola")))
    )
  )

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite(getClass.getSimpleName)(test: _*).provideCustomLayerShared(s3Layer) @@
      sequential @@
      beforeAll(
        ZIO.acquireRelease(ZIO.attemptBlockingIO(container.start()))(_ => ZIO.succeedBlocking(container.close()))
      )

}
