package io.github.mvillafuertem.advanced.phantom.types

import io.github.mvillafuertem.advanced.phantom.types.Chef.{Food, Pizza}
import org.scalatest.{FlatSpecLike, Matchers}

/**
 * @author Miguel Villafuerte
 */
final class ChefSpec extends FlatSpecLike with Matchers {

  behavior of "Chef"

  it should "apply" in {

    // G I V E N

    // W H E N
    val build = new Chef[Pizza.EmptyPizza]()
      .addCheese("mozzarella")
      .addDough()
      .addTopping("olives")
      .build

    // T H E N
    build shouldBe a[Food]
    build shouldBe Food(Seq("mozzarella", "dough", "olives"))

  }

}
