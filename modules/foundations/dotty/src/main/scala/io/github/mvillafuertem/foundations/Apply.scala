package io.github.mvillafuertem.foundations

trait Apply[F[_]] extends Functor[F] {
  def ap[A, B](fa: F[A])(ff: F[A => B]): F[B]
}

object Apply {

  def apply[F[_] : Apply] : Apply[F] = implicitly

  given intOptionApply: Apply[Option] with {
    def ap[A, B](fa: Option[A])(f: Option[A => B]): Option[B] = f match {
    case Some(ff) => fa.map(ff)
    case None => None
  }

    def map[A, B](a: Option[A])(f: A => B): Option[B] = a.map(f)
  }


  def test(): Unit = {

    import Apply.given
    val result = Apply[Option].ap[Int, Int](Some(3))(Some(_ * 10))

    println(result)
  }

}
