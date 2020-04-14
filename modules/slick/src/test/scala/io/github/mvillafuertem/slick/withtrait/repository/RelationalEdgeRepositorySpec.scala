package io.github.mvillafuertem.slick.withtrait.repository

import java.sql.SQLIntegrityConstraintViolationException

import io.github.mvillafuertem.slick.withtrait.configuration.InfrastructureConfigurationSpec
import io.github.mvillafuertem.slick.withtrait.model.{EdgeDBO, VertexDBO}
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.{FiniteDuration, _}
import scala.concurrent.{Await, Future}

final class RelationalEdgeRepositorySpec extends InfrastructureConfigurationSpec
  with AsyncFlatSpecLike
  with Matchers
  with BeforeAndAfterEach
  with OptionValues {

  import RelationalEdgeRepositorySpec._

  override def beforeEach(): Unit = {
    Await.result(relationalRepositories.edgeRepository.deleteAll(), timeout)
    Await.result(relationalRepositories.vertexRepository.deleteAll(), timeout)
  }

  behavior of "Edge Repository"

  val edgeSeq: Seq[EdgeDBO] = Seq(edgeOne, edgeTwo)

  it should "insert an edge into the database" in {
    val result = for {
      v1Id <- relationalRepositories.vertexRepository.insert(vertexOne)
      v2Id <- relationalRepositories.vertexRepository.insert(vertexTwo)
      eid <- relationalRepositories.edgeRepository.insert(EdgeDBO(v1Id, v2Id, metaModel))
    } yield eid

    result map { id => assert(id > 0) }
  }

  it should "fail inserting a duplicate edge into the database" in {

    recoverToSucceededIf[SQLIntegrityConstraintViolationException] {
      for {
        v1Id <- relationalRepositories.vertexRepository.insert(vertexOne)
        v2Id <- relationalRepositories.vertexRepository.insert(vertexTwo)
        _ <- relationalRepositories.edgeRepository.insert(EdgeDBO(v1Id, v2Id, metaModel))
        _ <- relationalRepositories.edgeRepository.insert(EdgeDBO(v1Id, v2Id, metaModel))
      } yield ()
    }
  }

  it should "fail inserting an edge with missing start vertex into the database" in {

    recoverToSucceededIf[SQLIntegrityConstraintViolationException] {
      for {
        v1Id <- relationalRepositories.vertexRepository.insert(vertexOne)
        _ <- relationalRepositories.edgeRepository.insert(EdgeDBO(0L, v1Id, metaModel))
      } yield ()
    }
  }


  it should "fail inserting an edge with missing end vertex into the database" in {

    recoverToSucceededIf[SQLIntegrityConstraintViolationException] {
      for {
        v1Id <- relationalRepositories.vertexRepository.insert(vertexOne)
        _ <- relationalRepositories.edgeRepository.insert(EdgeDBO(v1Id, 0, metaModel))
      } yield ()
    }
  }


  it should "find an edge by its start and end vertexes" in {
    val result = for {
      v1Id <- relationalRepositories.vertexRepository.insert(vertexOne)
      v2Id <- relationalRepositories.vertexRepository.insert(vertexTwo)
      _ <- relationalRepositories.edgeRepository.insert(EdgeDBO(v1Id, v2Id, metaModel))
      edge <- relationalRepositories.edgeRepository.findByStartAndEndVertexIds(v1Id, v2Id)
    } yield (edge, v1Id, v2Id)

    result map { tuple =>
      assert(tuple._1.startVertexId === tuple._2)
      assert(tuple._1.endVertexId === tuple._3)
    }
  }

  it should "insert a sequence of edges into the database" in {
    val actual: Future[Seq[Long]] = for {
      v1Id <- relationalRepositories.vertexRepository.insert(vertexOne)
      v2Id <- relationalRepositories.vertexRepository.insert(vertexTwo)
      vs <- relationalRepositories.edgeRepository.insert(Seq(EdgeDBO(v1Id, v1Id, metaModel), EdgeDBO(v2Id, v2Id, metaModel)))
    } yield vs

    actual map { vs =>
      assert(vs.nonEmpty)
      assert(vs.size == edgeSeq.size)
      assert(vs.forall(_ > 0L))
    }
  }

  it should "update an edge from the database" in {
    val result = for {
      v1Id <- relationalRepositories.vertexRepository.insert(vertexOne)
      v2Id <- relationalRepositories.vertexRepository.insert(vertexTwo)
      eid <- relationalRepositories.edgeRepository.insert(EdgeDBO(v1Id, v1Id, metaModel))
      _ <- relationalRepositories.edgeRepository.update(EdgeDBO(v1Id, v2Id, metaModel, Option(eid)))
      updated <- relationalRepositories.edgeRepository.findById(eid)
    } yield (eid, updated.value, v1Id, v2Id)

    result map { tuple =>
      assert(tuple._2.id.contains(tuple._1)) // edge id
      assert(tuple._2 == EdgeDBO(tuple._3, tuple._4, metaModel, Option(tuple._1)))
    }
  }

  it should "delete an edge from the database" in {
    val result = for {
      v1Id <- relationalRepositories.vertexRepository.insert(vertexOne)
      v2Id <- relationalRepositories.vertexRepository.insert(vertexTwo)
      eid <- relationalRepositories.edgeRepository.insert(EdgeDBO(v1Id, v2Id, metaModel))
      n <- relationalRepositories.edgeRepository.delete(eid)
      count <- relationalRepositories.edgeRepository.count()
    } yield (n, count)

    result map { tuple =>
      assert(tuple._1 == 1)
      assert(tuple._2 == 0)
    }
  }

  it should "retrieve all edges from the database" in {
    val result: Future[Seq[EdgeDBO]] = for {
      v1Id <- relationalRepositories.vertexRepository.insert(vertexOne)
      v2Id <- relationalRepositories.vertexRepository.insert(vertexTwo)
      _ <- relationalRepositories.edgeRepository.insert(Seq(EdgeDBO(v1Id, v1Id, metaModel), EdgeDBO(v2Id, v2Id, metaModel)))
      vs <- relationalRepositories.edgeRepository.findAll()
    } yield vs

    result map { seq =>
      assert(seq.size == 2)
    }
  }

  it should "find an edge list by its start vertex id" in {
    val result: Future[Seq[EdgeDBO]] = for {
      v1Id <- relationalRepositories.vertexRepository.insert(vertexOne)
      v2Id <- relationalRepositories.vertexRepository.insert(vertexTwo)
      _ <- relationalRepositories.edgeRepository.insert(
        Seq(EdgeDBO(v1Id, v1Id, metaModel), EdgeDBO(v1Id, v2Id, metaModel), EdgeDBO(v2Id, v2Id, metaModel)))
      vs <- relationalRepositories.edgeRepository.findByStartVertexId(v1Id)
    } yield vs

    result map { seq =>
      assert(seq.size == 2)
    }
  }

  it should "find an edge list by its end vertex id" in {
    val result: Future[Seq[EdgeDBO]] = for {
      v1Id <- relationalRepositories.vertexRepository.insert(vertexOne)
      v2Id <- relationalRepositories.vertexRepository.insert(vertexTwo)
      _ <- relationalRepositories.edgeRepository.insert(
        Seq(EdgeDBO(v1Id, v1Id, metaModel), EdgeDBO(v2Id, v2Id, metaModel), EdgeDBO(v1Id, v2Id, metaModel)))
      vs <- relationalRepositories.edgeRepository.findByEndVertexId(v1Id)
    } yield vs

    result map { seq =>
      assert(seq.size == 1)
    }
  }

}

object RelationalEdgeRepositorySpec {

  val timeout: FiniteDuration = 5.second

  val tenantIdOne = 1111L
  val assetIdOne = 5555L
  val vertexOne = VertexDBO(tenantIdOne, assetIdOne)

  val tenantIdTwo = 2222L
  val assetIdTwo = 6666L
  val vertexTwo = VertexDBO(tenantIdTwo, assetIdTwo)


  var startVertexIdOne: Long = tenantIdOne
  var endVertexIdOne: Long = tenantIdTwo
  val metaModel = "{ ...json... }"

  val edgeOne = EdgeDBO(startVertexIdOne, endVertexIdOne, metaModel)
  val edgeTwo = EdgeDBO(tenantIdTwo, tenantIdOne, metaModel)


}
