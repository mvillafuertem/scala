package io.github.mvillafuertem.todo.domain

import java.util.UUID

final case class ToDo(title: String, content: String, timestamp: Long, id: UUID = UUID.randomUUID())