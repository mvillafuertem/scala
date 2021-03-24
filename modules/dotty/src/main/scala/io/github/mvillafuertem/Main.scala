package io.github.mvillafuertem

import io.github.mvillafuertem.dotty._
import io.github.mvillafuertem.foundations.{ Functor, Monoid, PlainFunctionalProgramming, Semigroup }

object Main {

  def main(args: Array[String]): Unit = {

    runExample("Trait Params")(TraitParams.test())

    runExample("Enum Types")(EnumTypes.test())

    runExample("Context Functions")(ContextFunctions.test())

    runExample("Given Instances")(GivenInstances.test())

    runExample("Conversion")(Conversion.test())

    runExample("Union Types")(UnionTypes.test())

    runExample("Intersection Types")(IntersectionTypes.test())

    runExample("Type Lambda")(TypeLambdas.test())

    runExample("Multiversal Equality")(MultiversalEquality.test())

    runExample("Parameter Untupling")(ParameterUntupling.test())

    runExample("Structural Types")(StructuralTypes.test())

    runExample("Pattern Matching")(PatternMatching.test())

    runExample("PlainFunctionalProgramming")(PlainFunctionalProgramming.test())

    runExample("Semigroup")(Semigroup.test())

    runExample("Monoid")(Monoid.test())

    runExample("Functor")(Functor.test())

  }

  private def runExample(name: String)(f: => Unit) = {
    println(Console.MAGENTA + s"$name example:" + Console.RESET)
    f
    println()
  }

}
