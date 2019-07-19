package io.github.mvillafuertem.slick.stream.repository

import io.github.mvillafuertem.slick.stream.configuration.UserConfiguration
import io.github.mvillafuertem.slick.stream.configuration.UserConfiguration.userRepository._
import io.github.mvillafuertem.slick.stream.configuration.UserConfiguration.userRepository.profile.api._
import io.github.mvillafuertem.slick.stream.domain.User
import org.scalatest.{AsyncFlatSpec, BeforeAndAfterEach, Matchers, OptionValues}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class RelationalUserRepositorySpec
    extends AsyncFlatSpec
    with Matchers
    with BeforeAndAfterEach
    with OptionValues {

  private val userRepository: RelationalUserRepository =
    UserConfiguration.userRepository

  override protected def beforeEach(): Unit =
    Await.result(userRepository.userTable.schema.create, 5 seconds)

  override protected def afterEach(): Unit =
    Await.result(userRepository.userTable.schema.drop, 5 seconds)

  behavior of "RelationalUserRepositorySpec"

  it should "insert" in {

    // G I V E N
    val user: User = User(1234567890L, "Pepe", "Pipo")

    // W H E N
    val actual = for {
      result <- userRepository.insert(user)
    } yield result

    // T H E N
    actual.map { result =>
      result shouldBe 1
    }

  }

  it should "findAll" in {

    // G I V E N
    val user: User = User(1234567890L, "Pepe", "Pipo")

    // W H E N
    val actual = for {
      _ <- userRepository.insert(user)
      _ <- userRepository.insert(user)
      result <- userRepository.findAll()
    } yield result

    // T H E N
    actual.map { result =>
      result.size shouldBe 2
    }

  }

  it should "findById" in {

    // G I V E N
    val user: User = User(1234567890L, "Pepe", "Pipo")

    // W H E N
    val actual = for {
      _ <- userRepository.insert(user)
      _ <- userRepository.insert(user)
      result <- userRepository.findById(2)
    } yield result

    // T H E N
    actual.map { result =>
      result.value.id shouldBe Some(2)
    }

  }

  it should "print schema" in {

    val schema = userRepository
      .schema()
      .createStatements
      .mkString("\n")

    info(schema)

    Future.successful("").map(result => result shouldBe "")
  }

}
