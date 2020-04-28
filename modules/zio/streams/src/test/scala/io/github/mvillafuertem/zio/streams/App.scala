package io.github.mvillafuertem.zio.streams

import zio.stream.{ Stream, ZStreamChunk }
import zio.test.Assertion.equalTo
import zio.test.environment.TestEnvironment
import zio.test._
import zio.{ Chunk, Queue, Semaphore, ZIO }

object App extends DefaultRunnableSpec {

  def program(queue: Queue[Int]): Int => ZIO[Any, Nothing, Boolean] =
    i => queue.offer(i)

  override def spec: ZSpec[TestEnvironment, Any] =
    suite(getClass.getSimpleName)(
      testM("StreamMapMPar") {
        assertM(
          for {
            topic    <- Queue.bounded[Int](4096)
            _        <- Stream.fromIterable(1 to 4096).mapMPar(2)(program(topic)).runCollect
            elements <- Stream.fromQueue(topic).take(4096).runCollect
          } yield elements
          // t h e n
        )(equalTo((1 to 4096).toList))
      },
      testM("equal to StreamMapMPar 10 but with semaphore") {
        assertM(
          for {
            permits  <- Semaphore.make(10)
            topic    <- Queue.bounded[Int](4096)
            a        = permits.withPermit(program(topic).apply(1))
            _        <- ZIO.collectAllPar(List.fill(4096)(a))
            elements <- Stream.fromQueue(topic).take(4096).runCollect
          } yield elements
          // t h e n
        )(equalTo(List.fill(4096)(1)))
      },
      testM("StreamChunk") {
        assertM(
          for {
            topic: Queue[Int] <- Queue.bounded[Int](4096)
            _ <- ZStreamChunk
                  .fromChunks(Chunk.fromIterable(1 to 4096))
                  .flattenChunks
                  .mapM(program(topic))
                  .runCollect
            elements <- Stream.fromQueue(topic).take(4096).runCollect
          } yield elements
          // t h e n
        )(equalTo((1 to 4096).toList))
      }
    ) @@ TestAspect.timed

  override def aspects: List[TestAspect[Nothing, TestEnvironment, Nothing, Any]] =
    List(TestAspect.executionStrategy(ExecutionStrategy.Sequential))

}
