package io.github.mvillafuertem.slick.stream.domain


case class User(userId: Long,
                      name: String,
                      username: String,
                      id: Option[Long] = None
                     )
