package io.github.mvillafuertem.slick.withtrait.model

final case class VertexDBO(tenantId: Long, assetId: Long, id: Option[Long] = None) extends Entity[VertexDBO, Long]
