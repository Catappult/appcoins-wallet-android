package cm.aptoide.skills.model

sealed class ReferralResult

object SuccessfulReferral : ReferralResult()

sealed class FailedReferral : ReferralResult() {
  object GenericError : FailedReferral()
  object NotEligibleError : FailedReferral()
  object NotFoundError : FailedReferral()
}
