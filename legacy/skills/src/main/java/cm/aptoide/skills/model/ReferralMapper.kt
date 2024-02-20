package cm.aptoide.skills.model

import cm.aptoide.skills.util.getMessage
import com.google.gson.Gson
import javax.inject.Inject
import retrofit2.HttpException

class ReferralMapper @Inject constructor(private val jsonMapper: Gson) {
  fun mapHttpException(error: HttpException): ReferralResult {
    return when (error.code()) {
      404 -> FailedReferral.NotFoundError(error.getMessage())
      409 -> FailedReferral.NotEligibleError(error.getMessage())
      else -> FailedReferral.GenericError("Feature temporarily unavailable")
    }
  }
}
