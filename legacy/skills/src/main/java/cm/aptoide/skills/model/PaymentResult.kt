package cm.aptoide.skills.model

sealed class PaymentResult

object SuccessfulPayment : PaymentResult()

sealed class FailedPayment : PaymentResult() {
  data class GenericError(val errorMessage: String?) : FailedPayment()
  data class FraudError(val errorMessage: String?) : FailedPayment()
  object NoNetworkError : FailedPayment()
}
