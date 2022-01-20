package io.github.mvillafuertem.foundations

// https://www.youtube.com/watch?v=YXDm3WHZT5g
object PlainFunctionalProgramming {

  final case class Name(first: String, last: String)

  final case class Age(age: Int)

  final case class Person(name: Name, age: Age)

  final case class Config(name: String, age: Int)

  //  Solution 1:
  //  object Imperative {
  //    var config: Config = null
  //
  //    def readName: Name = {
  //      val parts = config.name.split(" ")
  //      require(parts.length >= 2)
  //      Name(parts(0), parts.tail.mkString(" "))
  //    }
  //    def readAge: Age = {
  //      val age = config.age
  //      require(1 <= age && age <= 150)
  //      Age(age)
  //    }
  //    def readPerson: Option[Person] =
  //      try Some(Person(readName, readAge))
  //      catch { case _: IllegalArgumentException => None }
  //
  //    def run(): Unit = {
  //      config = Config("John Doe", 20)
  //      println(readPerson)
  //      config = Config("Incognito", 20)
  //      println(readPerson)
  //    }
  //  }

  // Solution 2:
  //  object Imperative {
  //
  //    def readName(config: Config): Name = {
  //      val parts = config.name.split(" ")
  //      require(parts.length >= 2)
  //      Name(parts(0), parts.tail.mkString(" "))
  //    }
  //    def readAge(config: Config): Age = {
  //      val age = config.age
  //      require(1 <= age && age <= 150)
  //      Age(age)
  //    }
  //    def readPerson(config: Config): Option[Person] =
  //      try Some(Person(readName(config), readAge(config)))
  //      catch { case _: IllegalArgumentException => None }
  //
  //    def run(): Unit = {
  //      println(readPerson(Config("John Doe", 20)))
  //      println(readPerson(Config("Incognito", 20)))
  //    }
  //  }

  // Solution 3:
  //  object Imperative {
  //
  //    def readName(implicit config: Config): Name = {
  //      val parts = config.name.split(" ")
  //      require(parts.length >= 2)
  //      Name(parts(0), parts.tail.mkString(" "))
  //    }
  //    def readAge(implicit config: Config): Age = {
  //      val age = config.age
  //      require(1 <= age && age <= 150)
  //      Age(age)
  //    }
  //    def readPerson(implicit config: Config): Option[Person] =
  //      try Some(Person(readName, readAge))
  //      catch { case _: IllegalArgumentException => None }
  //
  //    def run(): Unit = {
  //      println(readPerson(Config("John Doe", 20)))
  //      println(readPerson(Config("Incognito", 20)))
  //    }
  //  }

  /**
   * Solution 4:
   *
   * Context Functions:
   *   - https://dotty.epfl.ch/docs/reference/contextual/context-functions.html
   *   - https://www.scala-lang.org/blog/2016/12/07/implicit-function-types.html
   *
   * Comparing with Kleisli Triples
   *   - Essentially, Kleisli triples wrap the implicit reading in a reader Monad
   *   - Some advantage as implicit function types · The reading is abstract in a type
   *   - But much harder to get the plumbing correct
   *   - Monads are about sequencing, they have nothing to do with passing context
   *
   * Strategic Scala Style: Principle of Least Power
   *   - https://www.lihaoyi.com/post/StrategicScalaStylePrincipleofLeastPower.html
   */
  object Imperative {

    import Configs._
    import Exceptions._

    // Algebraic Effects
    def readName: Posibility[Configured[Name]] = {
      val parts = config.name.split(" ")
      require(parts.length >= 2)
      Name(parts(0), parts.tail.mkString(" "))
    }

    def readAge: Posibility[Configured[Age]] = {
      val age = config.age
      require(1 <= age && age <= 150)
      Age(age)
    }

    def readPerson: Configured[Option[Person]] =
      attempt(Some(Person(readName, readAge))).onError(None)

    def run(): Unit = {
      println(readPerson(using Config("John Doe", 20)))
      println(readPerson(using Config("Incognito", 20)))
    }
  }

  object Configs {
    type Configured[T] = Config ?=> T
    def config: Configured[Config] = implicitly[Config]
  }

  object Exceptions {

    private class E extends Exception

    class CanThrow private[Exceptions] () {
      private[Exceptions] def throwE = throw new E
    }

    type Posibility[T] = CanThrow ?=> T

    def require(p: Boolean)(using ct: CanThrow): Unit = if (!p) ct.throwE
    def attempt[T](op: Posibility[T])                 = new OnError[T](op)

    class OnError[T](op: Posibility[T]) {
      def onError(fallback: => T): T =
        try op(using new CanThrow)
        catch { case _: E => fallback }
    }

  }

  def test(): Unit = {
    import io.github.mvillafuertem.foundations.PlainFunctionalProgramming.Imperative
    Imperative.run()
  }

}
