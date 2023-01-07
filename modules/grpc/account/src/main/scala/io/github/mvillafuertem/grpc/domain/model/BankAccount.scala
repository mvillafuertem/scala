package io.github.mvillafuertem.grpc.domain.model

final case class BankAccount(
  iban: String,
  bank: String,
  branch: String,
  control: String,
  number: String
)
