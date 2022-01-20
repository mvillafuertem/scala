package io.github.mvillafuertem.dotty
import scala.util.{ Success, Try }

/**
 * Implied Instances: https://dotty.epfl.ch/docs/reference/contextual/givens.html
 */
object GivenInstances:

  sealed trait StringParser[A]:
    def parse(s: String): Try[A]

  object StringParser:

    def apply[A](using parser: StringParser[A]): StringParser[A] = parser

    private def baseParser[A](f: String ⇒ Try[A]): StringParser[A] = new StringParser[A] {
      override def parse(s: String): Try[A] = f(s)
    }

    given stringParser: StringParser[String] = baseParser(Success(_))
    given intParser: StringParser[Int]       = baseParser(s ⇒ Try(s.toInt))

    given optionParser[A](using parser: => StringParser[A]): StringParser[Option[A]] = new StringParser[Option[A]] {
      override def parse(s: String): Try[Option[A]] = s match
        case ""  ⇒ Success(None)                      // implicit parser not used.
        case str ⇒ parser.parse(str).map(x ⇒ Some(x)) // implicit parser is evaluated at here
    }

  def test(): Unit =
    println(summon[StringParser[Option[Int]]].parse("21"))
    println(summon[StringParser[Option[Int]]].parse(""))
    println(summon[StringParser[Option[Int]]].parse("21a"))
    println(summon[StringParser[Option[Int]]](using StringParser.optionParser[Int]).parse("42"))
