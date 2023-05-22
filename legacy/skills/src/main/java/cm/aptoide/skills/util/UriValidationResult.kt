package cm.aptoide.skills.util

import com.appcoins.wallet.core.network.eskills.model.EskillsPaymentData

sealed class UriValidationResult {
  data class Valid(val paymentData: EskillsPaymentData) : UriValidationResult()
  data class Invalid(val requestCode: Int) : UriValidationResult()
}