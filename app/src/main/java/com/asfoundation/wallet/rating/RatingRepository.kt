package com.asfoundation.wallet.rating

import com.appcoins.wallet.commons.Logger
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.rating.network.WalletFeedbackBody
import io.reactivex.Single
import okhttp3.ResponseBody
import repository.RatingSharedPreferences
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import javax.inject.Inject

class RatingRepository @Inject constructor(
  private val ratingSharedPreferences: RatingSharedPreferences,
  private val walletFeedbackApi: WalletFeedbackApi,
  private val logger: Logger
) {

  fun saveEnoughSuccessfulTransactions() =
    ratingSharedPreferences.saveEnoughSuccessfulTransactions()

  fun hasEnoughSuccessfulTransactions() = ratingSharedPreferences.hasEnoughSuccessfulTransactions()

  /**
   * This method is similar to hasSeenDialog but it differs in use:
   *  - hasSeenDialog should return true as soon as the entry fragment is displayed for the first time
   *  - This methood should only return true once the rating flow is seen and closed for the first time
   */
  fun isNotFirstTime() = hasSeenDialog() && getRemindMeLaterDate() > 0

  fun getRemindMeLaterDate() = ratingSharedPreferences.getRemindMeLaterDate()

  fun hasSeenDialog() = ratingSharedPreferences.hasSeenDialog()

  fun setImpression() = ratingSharedPreferences.setImpression()

  fun setRemindMeLater() = ratingSharedPreferences.setRemindMeLater()

  fun sendFeedback(walletAddress: String, feedbackText: String): Single<Boolean> {
    val body = WalletFeedbackBody(WalletFeedbackBody.Ticket("Wallet Feedback - $walletAddress",
        WalletFeedbackBody.Comment(feedbackText)))
    return walletFeedbackApi.sendFeedback("Basic ${BuildConfig.FEEDBACK_ZENDESK_API_KEY}", body)
        .map { response -> response.code() == 200 || response.code() == 204 }
        .onErrorReturn { e ->
          logger.log("RatingRepository", e)
          false
        }
  }

  interface WalletFeedbackApi {
    @POST("tickets.json")
    fun sendFeedback(@Header("Authorization") authorization: String,
                     @Body feedback: WalletFeedbackBody): Single<Response<ResponseBody>>
  }
}