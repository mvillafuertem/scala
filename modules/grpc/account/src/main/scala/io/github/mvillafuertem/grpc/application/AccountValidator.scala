package io.github.mvillafuertem.grpc.application

import cats.data.ValidatedNel
import cats.implicits.{ catsSyntaxTuple5Semigroupal, catsSyntaxValidatedId }
import io.github.mvillafuertem.grpc.account.Request
import io.github.mvillafuertem.grpc.domain.AccountVerification.{ BankIsInvalid, BranchIsInvalid, ControlIsInvalid, IbanIsInvalid, NumberIsInvalid }
import io.github.mvillafuertem.grpc.domain.{ AccountVerification, BankAccount }

object AccountValidator {

  type ValidationResult[A] = ValidatedNel[AccountVerification, A]

  private def validateNumber(number: String): ValidationResult[String] =
    if ("""^[0-9]{10}$""".r.matches(number)) number.validNel else NumberIsInvalid.invalidNel

  private def validateControl(control: String): ValidationResult[String] =
    if ("""^[0-9]{2}$""".r.matches(control)) control.validNel
    else ControlIsInvalid.invalidNel

  private def validateBranch(branch: String): ValidationResult[String] =
    if ("""^[0-9]{4}$""".r.matches(branch)) branch.validNel else BranchIsInvalid.invalidNel

  private def validateBank(bank: String): ValidationResult[String] =
    if ("""^[0-9]{4}$""".r.matches(bank)) bank.validNel else BankIsInvalid.invalidNel

  private def validateIban(iban: String): ValidationResult[String] =
    if ("""^[A-Z]{2}[0-9]{2}$""".r.matches(iban)) iban.validNel else IbanIsInvalid.invalidNel

  def validate(request: Request): ValidationResult[BankAccount] =
    (
      validateIban(request.iban),
      validateBank(request.bank),
      validateBranch(request.branch),
      validateControl(request.control),
      validateNumber(request.number)
    ).mapN(BankAccount)

}
