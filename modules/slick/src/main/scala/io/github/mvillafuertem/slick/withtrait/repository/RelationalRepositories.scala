package io.github.mvillafuertem.slick.withtrait.repository

import io.github.mvillafuertem.slick.withtrait.model.{ EdgeDBO, VertexDBO }
import io.github.mvillafuertem.slick.withtrait.{ EdgeRepository, VertexRepository }
import slick.jdbc.JdbcBackend

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait RelationalRepositories extends RelationalInfrastructure {
  self: Profile =>

  import profile.api._

  final class Vertexes(tag: Tag) extends RelationalTable[VertexDBO](tag, "VERTEXES") {
    // C O L U M N S
    def tenantId = column[Long]("TENANT_ID")

    def assetId = column[Long]("ASSET_ID")

    // D E F A U L T  P R O J E C T I O N
    def * = (tenantId, assetId, id.?).mapTo[VertexDBO]

    // I N D E X
    def tenantAssetIndex = index("tenant_asset_index", (tenantId, assetId), unique = true)
  }

  lazy val vertexTable = TableQuery[Vertexes]

  final class Edges(tag: Tag) extends RelationalTable[EdgeDBO](tag, "EDGES") {
    // C O L U M N S
    def startVertexId = column[Long]("START_VERTEX_ID")

    def endVertexId = column[Long]("END_VERTEX_ID")

    def metaModel = column[String]("META_MODEL")

    // D E F A U L T  P R O J E C T I O N
    def * = (startVertexId, endVertexId, metaModel, id.?).mapTo[EdgeDBO]

    // F O R E I G N  K E Y S
    def startVertex = foreignKey("START_VERTEX_FK", startVertexId, vertexTable)(_.id)

    def endVertex = foreignKey("END_VERTEX_FK", endVertexId, vertexTable)(_.id)

    // I N D E X
    def startEndVertexIndex = index("start_end_vertex_index", (startVertexId, endVertexId), unique = true)

  }

  lazy val edgeTable = TableQuery[Edges]

  /**
   *
   * @param db database back-end definition for the Vertex repository
   */
  class RelationalVertexRepository(val db: JdbcBackend#DatabaseDef)
      extends RelationalRepository[VertexDBO, Vertexes](db)
      with VertexRepository[Future, VertexDBO] {
    override lazy val entities = vertexTable

    /**
     * Searches for an Vertex Entity into the repository.
     *
     * @param tenantId tenant identifier
     * @param assetId  device identifier
     * @return a Vertex if exists and is unique, otherwise returns a domain error
     */
    def findByCode(tenantId: Long, assetId: Long): Future[VertexDBO] =
      vertexTable.filter(vertex => vertex.tenantId === tenantId && vertex.assetId === assetId).result.flatMap { xs =>
        xs.length match {
          case 1 => DBIO.successful(xs.head)
          //TODO case _ => DBIO.failed(AssetComposerException(NonExistentEntityError))
        }
      }

  }

  /**
   *
   * @param db database back-end definition for the Edge repository
   */
  class RelationalEdgeRepository(val db: JdbcBackend#DatabaseDef) extends RelationalRepository[EdgeDBO, Edges](db) with EdgeRepository[Future, EdgeDBO] {

    override lazy val entities = edgeTable

    def findByStartVertexId(startVertexId: Long): Future[Seq[EdgeDBO]] =
      edgeTable.filter(edge => edge.startVertexId === startVertexId).result

    def findByEndVertexId(endVertexId: Long): Future[Seq[EdgeDBO]] =
      edgeTable.filter(edge => edge.endVertexId === endVertexId).result

    def findByStartAndEndVertexIds(startVertexId: Long, endVertexId: Long): Future[EdgeDBO] =
      edgeTable.filter(edge => edge.startVertexId === startVertexId && edge.endVertexId === endVertexId).result.flatMap { xs =>
        xs.length match {
          case 1 => DBIO.successful(xs.head)
          //TODO case _ => DBIO.failed(AssetComposerException(NonExistentEntityError))
        }
      }
  }

}
