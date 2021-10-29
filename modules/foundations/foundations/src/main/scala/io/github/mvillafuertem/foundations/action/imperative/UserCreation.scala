package io.github.mvillafuertem.foundations.action.imperative

import java.time.format.DateTimeFormatter
import java.time.{ Instant, LocalDate }
import scala.io.StdIn

// Run the App using the green arrow next to object (if using IntelliJ)
// or run `sbt` in the terminal to open it in shell mode, then type:
// exercises/runMain exercises.action.imperative.UserCreationApp
object UserCreationApp extends App {
  import UserCreation._

  readUser()
}

object UserCreation {
  val dateOfBirthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-uuuu")

  case class User(name: String, dateOfBirth: LocalDate, subscribedToMailingList: Boolean, createdAt: Instant)

  def readUser(): User = {
    println("What's your name?")
    val name        = StdIn.readLine()
    println("What's your date of birth? [dd-mm-yyyy]")
    val dateOfBirth = LocalDate.parse(StdIn.readLine(), dateOfBirthFormatter)
    val now         = Instant.now()
    val user        = User(name, dateOfBirth, false, now)
    println(s"User is $user")
    user
  }

  // 1. Implement `readSubscribeToMailingList` which asks if the user wants to
  // subscribe to our mailing list. They can answer "Y" for yes or "N" for No.
  // If the user enters something else, `readSubscribeToMailingList` throws an exception.
  // For example,
  // [Prompt] Would you like to subscribe to our mailing list? [Y/N]
  // [User] N
  // Returns false. But,
  // [Prompt] Would you like to subscribe to our mailing list? [Y/N]
  // [User] Nope
  // Throws an exception.
  // Note: You can read a user input using `StdIn.readLine()`.
  // Note: You can use `throw new IllegalArgumentException("...")` to throw an exception.

  /**
   * Si nos fijamos en el Test, solo estamos probando la forma en la que parsea los datos, pero no la interaccion con el usuario y esto supone no probar
   * completamente nuestra aplicacion. Para poder probar la interaccion del usuario necesitamos inyectar una Console interface a nuestro método.
   */
  def readSubscribeToMailingList(): Boolean = {
    println("Would you like to subscribe to our mailing list? [Y/N]")
    val line = StdIn.readLine()
    parseYesNo(line)
  }

  def formatYesNo(yesNo: Boolean): String =
    if (yesNo) "Y" else "N"

  def parseYesNo(line: String): Boolean =
    line match {
      case "Y"   => true
      case "N"   => false
      case other => throw new IllegalArgumentException(s"""Expected "Y" or "N" but received $other""")
    }

  // 2. How can we test `readSubscribeToMailingList`?
  // We cannot use example-based tests or property-based tests
  // because `readSubscribeToMailingList` depends on the
  // standard input `StdIn`.
  // Implement a new version of `readSubscribeToMailingList` which uses an instance
  // of `Console` to read/write lines.
  // Then, try to test this version using property-based testing.
  // Note: Check the `Console` companion object.
  // Bonus: Try to write a property-based test for `readSubscribeToMailingList`
  def readSubscribeToMailingList(console: Console): Boolean = {
    console.writeLine("Would you like to subscribe to our mailing list? [Y/N]")
    val line = console.readLine()
    parseYesNo(line)
  }

  // 3. Implement `readDateOfBirth` which asks the date of birth of the user.
  // User must answer using the format `dd-mm-yyyy`, e.g. "18-03-2001" for 18th of March 2001.
  // If they enter an invalid response, `readDateOfBirth` throws an exception.
  // For example,
  // [Prompt] What's your date of birth? [dd-mm-yyyy]
  // [User] 21-07-1986
  // Returns LocalDate.of(1986,7,21). But,
  // [Prompt] What's your date of birth? [dd-mm-yyyy]
  // [User] 1986/07/21
  // Throws an exception.
  // Note: You can use `LocalDate.parse` to parse a String into a LocalDate.
  // Note: You can use the formatter `dateOfBirthFormatter` (in scope).
  def readDateOfBirth(console: Console): LocalDate = {
    console.writeLine("What's your date of birth? [dd-mm-yyyy]")
    val line = console.readLine()
    LocalDate.parse(line, dateOfBirthFormatter)
  }

  def readName(console: Console): String = {
    console.writeLine("What's your name?")
    console.readLine()
  }

  // 4. Implement a testable version of `readUser`.
  // For example,
  // [Prompt] What's your name?
  // [User] Eda
  // [Prompt] What's your date of birth? [dd-mm-yyyy]
  // [User] 18-03-2001
  // [Prompt] Would you like to subscribe to our mailing list? [Y/N]
  // [User] Y
  // [Prompt] User is User(Eda,2001-03-18,true,2021-04-26T11:09:12.702261Z))
  // Returns User(
  //   name = "Eda",
  //   dateOfBirth = LocalDate.of(2001, 3, 18),
  //   subscribedToMailingList = true,
  //   createdAt = Instant.now()
  // )
  // Note: You will need to add `subscribedToMailingList: Boolean` field to `User`.
  // Note: How can you mock the current time? Check the `Clock` class in this package
  //       and update the signature of `readUser`.
  def readUser(console: Console, clock: Clock): User = {
    val name                    = readName(console)
    val dateOfBirth             = readDateOfBirth(console)
    val subscribedToMailingList = readSubscribeToMailingList(console)
    val user                    = User(name, dateOfBirth, subscribedToMailingList, clock.now())
    console.writeLine(s"User is $user")
    user
  }

  // ////////////////////////////////////////////
  // PART 2: Error handling
  // ////////////////////////////////////////////

  // 5. Implement `readSubscribeToMailingListRetry` which behaves like
  // `readSubscribeToMailingList` but retries if the user enters an invalid input.
  // This method also prints an error message when the attempt fails.
  // For example, readSubscribeToMailingListRetry(console, maxAttempt = 2)
  // [Prompt] Would you like to subscribe to our mailing list? [Y/N]
  // [User] Never
  // [Prompt] Incorrect format, enter "Y" for Yes or "N" for "No"
  // [Prompt] Would you like to subscribe to our mailing list? [Y/N]
  // [User] N
  // Returns false. But, readSubscribeToMailingListRetry(console, maxAttempt = 1)
  // [Prompt] Would you like to subscribe to our mailing list? [Y/N]
  // [User] Never
  // [Prompt] Incorrect format, enter "Y" for Yes or "N" for "No"
  // Throws an exception because the user only had 1 attempt and they entered an invalid input.
  // Note: `maxAttempt` must be greater than 0, if not you should throw an exception.
  // Note: You can implement the retry logic using recursion or a for/while loop. I suggest
  //       trying both possibilities.
  def readSubscribeToMailingListRetry(console: Console, maxAttempt: Int): Boolean =
    ???

  // 6. Implement `readDateOfBirthRetry` which behaves like
  // `readDateOfBirth` but retries when the user enters an invalid input.
  // For example: readDateOfBirth(dateOfBirthFormatter, maxAttempt = 2)
  // [Prompt] What's your date of birth? [dd-mm-yyyy]
  // [User] 21st of July
  // [Prompt] Incorrect format, for example enter "18-03-2001" for 18th of March 2001
  // [Prompt] What's your date of birth? [dd-mm-yyyy]
  // [User] 21-07-1986
  // Returns LocalDate.of(1986,7,21)
  // But, readDateOfBirth(dateOfBirthFormatter, maxAttempt = 1)
  // [Prompt] What's your date of birth? [dd-mm-yyyy]
  // [User] 21st of July
  // [Prompt] Incorrect format, for example enter "18-03-2001" for 18th of March 2001
  // Throws an exception because the user only had 1 attempt and they entered an invalid input.
  // Note: `maxAttempt` must be greater than 0, if not you should throw an exception.
  def readDateOfBirthRetry(console: Console, maxAttempt: Int): LocalDate =
    ???

  // 7. Update `readUser` so that it allows the user to make up to 2 mistakes (3 attempts)
  // when entering their date of birth and mailing list subscription flag.

  // ////////////////////////////////////////////
  // Bonus question (not covered by the videos)
  // ////////////////////////////////////////////

  // 8. Implement `readSubscribeToMailingListRetry` using a while-loop instead of a recursion.

  // 9. Transform the example based tests for `readSubscribeToMailingListRetry` into a property based test.
  // Step 1. Randomise Yes/No input. For example, generate random boolean and convert it to "Y" or "N".
  // Step 2. Randomise invalid input. For example, generate a random String that's not "Y" or "N".
  // Step 3. Randomise `maxAttempt`. For example, a number between 1 and 20.
  // Step 4. Randomise the number of invalid inputs. For example, generate a List of invalid inputs
  //         using `Gen.listOf`.
  // Step 5. Check that the method returns a success if `maxAttempt > # of invalid inputs`,
  //         otherwise, it throws an exception.

  // 10. Write property based tests for `readDateOfBirthRetry` and `readUser`.

}
