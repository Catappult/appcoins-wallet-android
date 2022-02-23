package cm.aptoide.skills.util

sealed class UriValidationResult {
  data class Valid(val paymentData: EskillsPaymentData) : UriValidationResult()
  data class Invalid(val requestCode: Int) : UriValidationResult()
}