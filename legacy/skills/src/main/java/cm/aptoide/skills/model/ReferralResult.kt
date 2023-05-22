package cm.aptoide.skills.model

import com.appcoins.wallet.core.network.eskills.model.ReferralResponse

sealed class ReferralResult

data class SuccessfulReferral(val referral: ReferralResponse): ReferralResult()

sealed class FailedReferral : ReferralResult() {
  data class GenericError(val detail: String) : FailedReferral()
  data class NotEligibleError(val detail: String) : FailedReferral()
  data class NotFoundError(val detail: String) : FailedReferral()
}
