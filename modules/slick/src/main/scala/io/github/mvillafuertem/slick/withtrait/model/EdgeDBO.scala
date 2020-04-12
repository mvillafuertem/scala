package io.github.mvillafuertem.slick.withtrait.model

final case class EdgeDBO(startVertexId: Long,
                         endVertexId: Long,
                         metaModel: String,
                         id: Option[Long] = None) extends Entity[EdgeDBO, Long]
