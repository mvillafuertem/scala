package io.github.mvillafuertem.grpc.domain

import enumeratum.{ CirceEnum, Enum, EnumEntry }
import enumeratum.EnumEntry.UpperSnakecase

sealed trait AccountVerification extends EnumEntry with UpperSnakecase

object AccountVerification extends Enum[AccountVerification] with CirceEnum[AccountVerification] {

  final case object IbanIsInvalid extends AccountVerification

  final case object BankIsInvalid extends AccountVerification

  final case object BranchIsInvalid extends AccountVerification

  final case object ControlIsInvalid extends AccountVerification

  final case object NumberIsInvalid extends AccountVerification

  override def values: IndexedSeq[AccountVerification] = findValues

}
