package cm.aptoide.skills.model

import retrofit2.HttpException
import javax.inject.Inject

class ReferralMapper @Inject constructor() {
  fun mapHttpException(error: HttpException): ReferralResult {
    return when (error.code()) {
      404 -> FailedReferral.NotFoundError
      409 -> FailedReferral.NotEligibleError
      else -> FailedReferral.GenericError
    }
  }
}