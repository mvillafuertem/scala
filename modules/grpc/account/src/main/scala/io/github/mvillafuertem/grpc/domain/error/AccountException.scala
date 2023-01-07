package io.github.mvillafuertem.grpc.domain.error

final case class AccountException(error: AccountError) extends RuntimeException
