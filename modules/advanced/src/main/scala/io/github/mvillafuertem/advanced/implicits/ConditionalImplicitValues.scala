package io.github.mvillafuertem.advanced.implicits

import io.github.mvillafuertem.advanced.implicits.ConditionalImplicitValues.ConditionalImplicit

import scala.concurrent.ExecutionContext

case class ConditionalImplicitValues(a: Int = 0, b: Int = 0) { self =>

  def n1(a: Int): ConditionalImplicitValues = self.copy(a = a)

  def n2(a: Int): ConditionalImplicitValues = self.copy(b = a)

  def run()(implicit ci: ConditionalImplicit): Int = a + b

}

object ConditionalImplicitValues {

  final class ConditionalImplicit()

  object ConditionalImplicit {

    /**
     * Esto viene a significar que te voy a crear una evidencia implicita,
     * siempre que encuentre una evidencia implicita de ExecutionContext, es decir,
     * está condicionado a que exista un implicito de ExecutionContext
     */
    implicit def conditionalImplicitFromExecutionContext(implicit ec: ExecutionContext): ConditionalImplicit = new ConditionalImplicit()

  }

}
