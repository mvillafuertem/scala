package io.github.mvillafuertem.foundations.generic

import io.github.mvillafuertem.foundations.generic.GenericFunction._
import org.scalacheck.Gen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import java.time.LocalDate
import java.time.format.DateTimeFormatter

final class GenericFunctionSpec extends AnyFlatSpec with Matchers with ScalaCheckDrivenPropertyChecks {

  ////////////////////
  // Exercise 1: Pair
  ////////////////////

  it should "Pair swap" in {
    forAll((first: String, second: String) => Pair(first, second).swap shouldBe Pair(second, first))
  }

  it should "Pair map" in {
    Pair(0, 1).map(identity) shouldBe Pair(0, 1)
  }

  it should "Pair decoded" in {
    decoded shouldBe Pair("Functional", "Programming")
  }

  it should "Pair zipWith" in {
    Pair(0, 2).zipWith(Pair(3, 4))(_ + _) shouldBe Pair(3, 6)
  }

  it should "Pair productNames" in {
    products shouldBe Pair(Product("Coffee", 2.5), Product("Plane ticket", 329.99))
  }

  it should "Pair map3" in {
    Pair(0, 2).map3(Pair(3, 4))(Pair(4, 6))(_ + _ + _) shouldBe Pair(7, 12)
  }

  ////////////////////////////
  // Exercise 2: Predicate
  ////////////////////////////

  def False[A]: Predicate[A] = Predicate(_ => false)
  def True[A]: Predicate[A]  = Predicate(_ => true)

  it should "Predicate &&" in {
    forAll { (eval: Int => Boolean, value: Int) =>
      val p1 = Predicate(eval)

      (p1 && False)(value) shouldBe false
      (p1 && True)(value) shouldBe p1(value)
    }
  }

  it should "Predicate ||" in {
    forAll { (eval: Int => Boolean, value: Int) =>
      val p1 = Predicate(eval)
      (p1 || False)(value) shouldBe p1(value)
      (p1 || True)(value) shouldBe true
    }
  }

  it should "Predicate flip" in {
    True.flip(()) shouldBe false
    False.flip(()) shouldBe true
  }

  it should "Predicate isValidUser" in {
    isValidUser(User("John", 20)) shouldBe true
    isValidUser(User("John", 17)) shouldBe false // user is not an adult
    isValidUser(User("john", 20)) shouldBe false // name is not capitalized
    isValidUser(User("x", 23)) shouldBe false    // name is too small
  }

  ////////////////////////////
  // Exercise 3: JsonDecoder
  ////////////////////////////

  it should "JsonDecoder UserId" in {
    userIdDecoder.decode("1234") shouldBe UserId(1234)
    userIdDecoder.decode("-1") shouldBe UserId(-1)
    assertThrows[NumberFormatException](userIdDecoder.decode("hello"))
  }

  it should "JsonDecoder UserId round-trip" in {
    forAll { (number: Int) =>
      val json = number.toString
      userIdDecoder.decode(json) shouldBe UserId(number)
    }
  }

  it should "JsonDecoder LocalDate" in {
    localDateDecoder.decode("\"2020-03-26\"") shouldBe LocalDate.of(2020, 3, 26)
    assertThrows[IllegalArgumentException](localDateDecoder.decode("2020-03-26"))
    assertThrows[IllegalArgumentException](localDateDecoder.decode("hello"))
  }

  val genLocalDate: Gen[LocalDate] = Gen.choose(LocalDate.MIN.toEpochDay, LocalDate.MAX.toEpochDay).map(LocalDate.ofEpochDay)
  //implicit val arbitraryLocalDate: Arbitrary[LocalDate] = Arbitrary(genLocalDate)
  it should "JsonDecoder LocalDate round-trip" in {
    forAll(genLocalDate) { (localDate: LocalDate) =>
      val json = s""""${DateTimeFormatter.ISO_LOCAL_DATE.format(localDate)}""""
      localDateDecoder.decode(json) shouldBe localDate
    }
  }

  it should "JsonDecoder weirdLocalDateDecoder" in {
    forAll(genLocalDate) { (localDate: LocalDate) =>
      val json1 = s""""${DateTimeFormatter.ISO_LOCAL_DATE.format(localDate)}""""
      val json2 = localDate.toEpochDay.toString
      weirdLocalDateDecoder.decode(json1) shouldBe localDate
      weirdLocalDateDecoder.decode(json2) shouldBe localDate
    }
  }

  it should "JsonDecoder Option" in {
    optionDecoder(stringDecoder).decode("null") shouldBe None
    optionDecoder(stringDecoder).decode("\"hello\"") shouldBe Some("hello")
  }

  it should "SafeJsonDecoder Int" in {
    SafeJsonDecoder.int.decode("1234") shouldBe Right(1234)
    SafeJsonDecoder.int.decode("hello") shouldBe Left("Invalid JSON Int: hello")
  }

  it should "SafeJsonDecoder orElse" in {
    val date = LocalDate.of(2020, 3, 26)
    SafeJsonDecoder.localDate.decode("\"2020-03-26\"") shouldBe Right(date)
    SafeJsonDecoder.localDate.decode("18347") shouldBe Right(date)
  }

}
