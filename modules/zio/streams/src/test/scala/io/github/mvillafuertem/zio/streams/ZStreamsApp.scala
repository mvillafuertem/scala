package io.github.mvillafuertem.zio.streams

import zio._
import zio.stream.ZStream
import zio.test.Assertion.equalTo
import zio.test._
import zio.test.environment.TestEnvironment

object ZStreamsApp extends DefaultRunnableSpec {

  def program(queue: Queue[Int]): Int => ZIO[Any, Nothing, Boolean] =
    i => queue.offer(i)

  override def spec: ZSpec[TestEnvironment, Any] =
    suite(getClass.getSimpleName)(
      testM("StreamMapMPar") {
        assertM(
          for {
            topic    <- Queue.bounded[Int](4096)
            _        <- ZStream.fromIterable(1 to 4096).mapMPar(2)(program(topic)).runCollect
            elements <- ZStream.fromQueue(topic).take(4096).runCollect
          } yield elements
          // t h e n
        )(equalTo((1 to 4096).toList))
      } @@ TestAspect.ignore,
      testM("equal to StreamMapMPar 10 but with semaphore") {
        assertM(
          for {
            permits  <- Semaphore.make(10)
            topic    <- ZQueue.bounded[Int](4096)
            a         = permits.withPermit(program(topic).apply(1))
            _        <- ZIO.collectAllPar(List.fill(4096)(a))
            elements <- ZStream.fromQueue(topic).take(4096).runCollect
          } yield elements
          // t h e n
        )(equalTo(List.fill(4096)(1)))
      },
      testM("StreamChunk") {
        assertM(
          for {
            topic: Queue[Int] <- Queue.bounded[Int](4096)
            _                 <- ZStream
                   .fromChunks(Chunk.fromIterable(1 to 4096))
                   .mapM(program(topic))
                   .runCollect
            elements          <- ZStream.fromQueue(topic).take(4096).runCollect
          } yield elements
          // t h e n
        )(equalTo((1 to 4096).toList))
      },
      testM("Array check fail") {
        assertM(
          for {
            splited <- Task.effect("a:b:c:d".split(":"))
            error   <- Task.effect(splited(4)).catchAll(exception => ZIO.fail(exception.getMessage)).run
          } yield error
          // t h e n
        )(Assertion.fails(equalTo("Index 4 out of bounds for length 4")))
      }
//      testM("Intersperse") {
//        assertM(
//          ZStream
//            .fromIterable(1 to 4096)
//            .map(_.toString)
//            .intersperse("@")
//            .runCollect
//          // t h e n
//        )(
//          equalTo(
//            (1 to 4096).toList
//              .map(_.toString)
//              .foldRight(List.empty[String]) {
//                case (element, Nil) => List(element)
//                case (element, out) => (element :: "@" :: out)
//              }
//          )
//        )
//      }
    ) @@ TestAspect.timed

  override def aspects: List[TestAspect[Nothing, TestEnvironment, Nothing, Any]] =
    List(TestAspect.executionStrategy(ExecutionStrategy.Sequential))

}
