package cm.aptoide.skills.model

sealed class ReferralResult

data class SuccessfulReferral(val referral: ReferralResponse): ReferralResult()

sealed class FailedReferral : ReferralResult() {
  data class GenericError(val detail: String) : FailedReferral()
  data class NotEligibleError(val detail: String) : FailedReferral()
  data class NotFoundError(val detail: String) : FailedReferral()
}
