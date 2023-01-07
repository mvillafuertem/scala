package io.github.mvillafuertem.grpc.domain.error

import enumeratum.EnumEntry.UpperSnakecase
import enumeratum.{CirceEnum, Enum, EnumEntry}

sealed trait AccountError extends EnumEntry with UpperSnakecase

object AccountError extends Enum[AccountError] with CirceEnum[AccountError] {

  final case object IbanIsInvalid extends AccountError

  final case object BankIsInvalid extends AccountError

  final case object BranchIsInvalid extends AccountError

  final case object ControlIsInvalid extends AccountError

  final case object NumberIsInvalid extends AccountError

  override def values: IndexedSeq[AccountError] = findValues

}
