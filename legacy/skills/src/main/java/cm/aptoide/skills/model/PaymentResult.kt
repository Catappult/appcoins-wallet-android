package cm.aptoide.skills.model

sealed class PaymentResult

object SuccessfulPayment : PaymentResult()

sealed class FailedPayment : PaymentResult() {
  object GenericError : FailedPayment()
  object FraudError : FailedPayment()
  object NoNetworkError : FailedPayment()
}
