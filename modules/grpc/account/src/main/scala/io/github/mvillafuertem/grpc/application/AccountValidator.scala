package io.github.mvillafuertem.grpc.application

import cats.implicits.{ catsSyntaxTuple5Semigroupal, catsSyntaxValidatedId }
import io.github.mvillafuertem.grpc.account.Request
import io.github.mvillafuertem.grpc.domain.error.AccountError.{ BankIsInvalid, BranchIsInvalid, ControlIsInvalid, IbanIsInvalid, NumberIsInvalid }
import io.github.mvillafuertem.grpc.domain.model.{ BankAccount, Validation }

object AccountValidator {

  private def validateNumber(number: String): Validation[String] =
    if ("""^[0-9]{10}$""".r.matches(number)) number.validNel else NumberIsInvalid.invalidNel

  private def validateControl(control: String): Validation[String] =
    if ("""^[0-9]{2}$""".r.matches(control)) control.validNel
    else ControlIsInvalid.invalidNel

  private def validateBranch(branch: String): Validation[String] =
    if ("""^[0-9]{4}$""".r.matches(branch)) branch.validNel else BranchIsInvalid.invalidNel

  private def validateBank(bank: String): Validation[String] =
    if ("""^[0-9]{4}$""".r.matches(bank)) bank.validNel else BankIsInvalid.invalidNel

  private def validateIban(iban: String): Validation[String] =
    if ("""^[A-Z]{2}[0-9]{2}$""".r.matches(iban)) iban.validNel else IbanIsInvalid.invalidNel

  def validate(request: Request): Validation[BankAccount] =
    (
      validateIban(request.iban),
      validateBank(request.bank),
      validateBranch(request.branch),
      validateControl(request.control),
      validateNumber(request.number)
    ).mapN(BankAccount)

}
