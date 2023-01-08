package io.github.mvillafuertem.cats.console.instances

import cats.effect.{Async, Sync}
import cats.syntax.flatMap._
import io.github.mvillafuertem.cats.console.Console

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

final class RemoteConsole[F[_]: Async] extends Console[F] {

  private def fromFuture[A](fa: F[Future[A]]): F[A] =
    fa.flatMap { future =>
      Async[F].async_ { cb =>
        future.onComplete {
          case Failure(exception) => cb(Left(exception))
          case Success(value)     => cb(Right(value))
        }
      }
    }

  override def putStrLn(string: String): F[Unit] =
    fromFuture(Sync[F].delay(??? /* HttpClient.post(string) */ ))

  override def readLn: F[String] =
    fromFuture(Sync[F].delay(??? /* HttpClient.get */ ))

}
