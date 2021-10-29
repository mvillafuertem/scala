package io.github.mvillafuertem.foundations.action.imperative

import io.github.mvillafuertem.foundations.action.DateGenerator
import io.github.mvillafuertem.foundations.action.imperative.UserCreation._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import java.time.{ Instant, LocalDate }
import scala.collection.mutable.ListBuffer
import scala.util.Try

class UserCreationSpec extends AnyFlatSpec with Matchers with ScalaCheckDrivenPropertyChecks {

  it should "parseYesNo" in {
    parseYesNo("Y") shouldBe true
    parseYesNo("N") shouldBe false
    val exception = intercept[IllegalArgumentException](parseYesNo("Never"))
    exception.getMessage shouldBe s"""Expected "Y" or "N" but received Never"""
  }

  it should "readSubscribeToMailingList example" in {
    val inputs  = ListBuffer("N")
    val outputs = ListBuffer.empty[String]
    val console = Console.mock(inputs, outputs)
    val result  = readSubscribeToMailingList(console)

    result shouldBe false
    outputs.toList shouldBe List("Would you like to subscribe to our mailing list? [Y/N]")
  }

  it should "readSubscribeToMailingList example PBT" in {
    forAll { (yesNo: Boolean) =>
      val inputs  = ListBuffer(formatYesNo(yesNo))
      val outputs = ListBuffer.empty[String]
      val console = Console.mock(inputs, outputs)
      val result  = readSubscribeToMailingList(console)

      result shouldBe yesNo
      outputs.toList shouldBe List("Would you like to subscribe to our mailing list? [Y/N]")
    }
  }

  it should "readSubscribeToMailingList example failure" in {
    val console = Console.mock(ListBuffer("Never"), ListBuffer())
    val result  = Try(readSubscribeToMailingList(console))

    result.isFailure shouldBe true
  }

  it should "readDateOfBirth example success" in {
    val console = Console.mock(ListBuffer("21-07-1986"), ListBuffer())
    val result  = readDateOfBirth(console)

    result shouldBe LocalDate.of(1986, 7, 21)
  }

  it should "readDateOfBirth example PBT" in {
    forAll(DateGenerator.dateGen) { (dateOfBirth: LocalDate) =>
      val console = Console.mock(ListBuffer(dateOfBirthFormatter.format(dateOfBirth)), ListBuffer())
      val result  = readDateOfBirth(console)

      result shouldBe dateOfBirth
    }
  }

  it should "readDateOfBirth example failure" in {
    val console = Console.mock(ListBuffer("21/07/1986"), ListBuffer())
    val result  = Try(readDateOfBirth(console))

    result.isFailure shouldBe true
  }

  it should "readUser example" in {
    val inputs  = ListBuffer("Eda", "18-03-2001", "Y")
    val outputs = ListBuffer.empty[String]
    val console = Console.mock(inputs, outputs)
    val now     = Instant.now()
    val clock   = Clock.constant(now)
    val result  = readUser(console, clock)

    val expected = User(
      name = "Eda",
      dateOfBirth = LocalDate.of(2001, 3, 18),
      subscribedToMailingList = true,
      createdAt = now
    )

    result shouldBe expected
  }

  // ////////////////////////////////////////////
  // PART 2: Error handling
  // ////////////////////////////////////////////

  ignore should "readSubscribeToMailingListRetry negative maxAttempt" in {
    val console = Console.mock(ListBuffer.empty[String], ListBuffer.empty[String])
    val result  = Try(readSubscribeToMailingListRetry(console, maxAttempt = -1))

    result.isFailure shouldBe true
  }

  ignore should "readSubscribeToMailingListRetry example success" in {
    val outputs = ListBuffer.empty[String]
    val console = Console.mock(ListBuffer("Never", "N"), outputs)
    val result  = readSubscribeToMailingListRetry(console, maxAttempt = 2)

    result shouldBe false
    outputs.toList shouldBe List(
      "Would you like to subscribe to our mailing list? [Y/N]",
      """Incorrect format, enter "Y" for Yes or "N" for "No"""",
      "Would you like to subscribe to our mailing list? [Y/N]"
    )
  }

  ignore should "readSubscribeToMailingListRetry example invalid input" in {
    val outputs = ListBuffer.empty[String]
    val console = Console.mock(ListBuffer("Never"), outputs)
    val result  = Try(readSubscribeToMailingListRetry(console, maxAttempt = 1))

    result.isFailure shouldBe true
    outputs.toList shouldBe List(
      "Would you like to subscribe to our mailing list? [Y/N]",
      """Incorrect format, enter "Y" for Yes or "N" for "No""""
    )

    // check that the error message is the same as `readSubscribeToMailingList`
    val console2 = Console.mock(ListBuffer("Never"), ListBuffer.empty[String])
    val result2  = Try(readSubscribeToMailingList(console2))
    result.failed.get.getMessage shouldBe result2.failed.get.getMessage
  }

  ignore should "readDateOfBirthRetry negative maxAttempt" in {
    val console = Console.mock(ListBuffer.empty[String], ListBuffer.empty[String])
    val result  = Try(readSubscribeToMailingListRetry(console, maxAttempt = -1))

    result.isFailure shouldBe true
  }

  ignore should "readDateOfBirthRetry example success" in {
    val outputs = ListBuffer.empty[String]
    val console = Console.mock(ListBuffer("July 21st 1986", "21-07-1986"), outputs)
    val result  = readDateOfBirthRetry(console, maxAttempt = 2)

    result shouldBe LocalDate.of(1986, 7, 21)
    outputs.toList shouldBe List(
      """What's your date of birth? [dd-mm-yyyy]""",
      """Incorrect format, for example enter "18-03-2001" for 18th of March 2001""",
      """What's your date of birth? [dd-mm-yyyy]"""
    )
  }

  ignore should "readDateOfBirthRetry example failure" in {
    val outputs        = ListBuffer.empty[String]
    val invalidAttempt = "July 21st 1986"
    val console        = Console.mock(ListBuffer(invalidAttempt), outputs)
    val result         = Try(readDateOfBirthRetry(console, maxAttempt = 1))

    result.isFailure shouldBe true
    outputs.toList shouldBe List(
      """What's your date of birth? [dd-mm-yyyy]""",
      """Incorrect format, for example enter "18-03-2001" for 18th of March 2001"""
    )

  }

}
