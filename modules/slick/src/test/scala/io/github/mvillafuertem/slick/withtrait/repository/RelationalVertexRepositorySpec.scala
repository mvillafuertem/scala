package io.github.mvillafuertem.slick.withtrait.repository

import java.sql.SQLIntegrityConstraintViolationException

import io.github.mvillafuertem.slick.withtrait.configuration.InfrastructureConfigurationSpec
import io.github.mvillafuertem.slick.withtrait.model.{ EdgeDBO, VertexDBO }
import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.{ BeforeAndAfterEach, OptionValues }

import scala.concurrent.duration.{ FiniteDuration, _ }
import scala.concurrent.{ Await, Future }

final class RelationalVertexRepositorySpec
    extends InfrastructureConfigurationSpec
    with AsyncFlatSpecLike
    with Matchers
    with BeforeAndAfterEach
    with OptionValues {

  import RelationalVertexRepositorySpec._

  override def beforeEach(): Unit = {
    Await.result(relationalRepositories.edgeRepository.deleteAll(), timeout)
    Await.result(relationalRepositories.vertexRepository.deleteAll(), timeout)
  }

  behavior of "Vertex Repository"

  val vertexSeq: Seq[VertexDBO] = Seq(vertexOne, vertexTwo)

  it should "insert a vertex into the database" in {
    val result = relationalRepositories.vertexRepository.insert(vertexOne)

    result map { id => assert(id > 0) }
  }

  it should "fail inserting a duplicate vertex into the database" in {

    recoverToSucceededIf[SQLIntegrityConstraintViolationException] {
      relationalRepositories.vertexRepository.insert(vertexOne)
      relationalRepositories.vertexRepository.insert(vertexOne)
    }
  }

  it should "find a vertex by its code" in {
    val result = for {
      _      <- relationalRepositories.vertexRepository.insert(vertexOne)
      vertex <- relationalRepositories.vertexRepository.findByCode(tenantIdOne, assetIdOne)
    } yield vertex

    result map { vertex =>
      assert(vertex.tenantId == tenantIdOne)
      assert(vertex.assetId == assetIdOne)
    }
  }

  it should "insert a sequence of vertexes into the database" in {

    val actual: Future[Seq[Long]] = for {
      vs <- relationalRepositories.vertexRepository.insert(vertexSeq)
    } yield vs

    actual map { vs =>
      assert(vs.nonEmpty)
      assert(vs.size == vertexSeq.size)
      assert(vs.forall(_ > 0L))
    }
  }

  it should "update a vertex from the database" in {
    val result = for {
      cid     <- relationalRepositories.vertexRepository.insert(vertexOne)
      _       <- relationalRepositories.vertexRepository.update(vertexTwo.copy(id = Option(cid)))
      updated <- relationalRepositories.vertexRepository.findById(cid)
    } yield (cid, updated.value)

    result map { tuple =>
      assert(tuple._2.id.contains(tuple._1))
      assert(tuple._2 == vertexTwo.copy(id = Option(tuple._1)))
    }
  }

  it should "delete a vertex from the database" in {
    val result = for {
      vid   <- relationalRepositories.vertexRepository.insert(vertexOne)
      n     <- relationalRepositories.vertexRepository.delete(vid)
      count <- relationalRepositories.vertexRepository.count()
    } yield (n, count)

    result map { tuple =>
      assert(tuple._1 == 1)
      assert(tuple._2 == 0)
    }
  }

  it should "retrieve all vertexes from the database" in {
    val result: Future[Seq[VertexDBO]] = for {
      _  <- relationalRepositories.vertexRepository.insert(vertexSeq)
      vs <- relationalRepositories.vertexRepository.findAll()
    } yield vs

    result map { seq => assert(seq.size == vertexSeq.size) }
  }

}

object RelationalVertexRepositorySpec {

  val timeout: FiniteDuration = 5.second

  val tenantIdOne = 1111L
  val assetIdOne  = 5555L
  val vertexOne   = VertexDBO(tenantIdOne, assetIdOne)

  val tenantIdTwo = 2222L
  val assetIdTwo  = 6666L
  val vertexTwo   = VertexDBO(tenantIdTwo, assetIdTwo)

  var startVertexIdOne: Long = tenantIdOne
  var endVertexIdOne: Long   = tenantIdTwo
  val metaModel              = "{ ...json... }"

  val edgeOne = EdgeDBO(startVertexIdOne, endVertexIdOne, metaModel)
  val edgeTwo = EdgeDBO(tenantIdTwo, tenantIdOne, metaModel)

}
