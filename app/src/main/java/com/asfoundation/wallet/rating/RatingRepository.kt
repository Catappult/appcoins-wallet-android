package com.asfoundation.wallet.rating

import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.rating.network.WalletFeedbackBody
import io.reactivex.Single
import okhttp3.ResponseBody
import com.appcoins.wallet.sharedpreferences.RatingPreferencesDataSource
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import javax.inject.Inject

class RatingRepository @Inject constructor(
  private val ratingPreferencesDataSource: RatingPreferencesDataSource,
  private val walletFeedbackApi: WalletFeedbackApi,
  private val logger: Logger
) {

  fun saveEnoughSuccessfulTransactions() =
    ratingPreferencesDataSource.saveEnoughSuccessfulTransactions()

  fun hasEnoughSuccessfulTransactions() =
    ratingPreferencesDataSource.hasEnoughSuccessfulTransactions()

  /**
   * This method is similar to hasSeenDialog but it differs in use:
   *  - hasSeenDialog should return true as soon as the entry fragment is displayed for the first time
   *  - This methood should only return true once the rating flow is seen and closed for the first time
   */
  fun isNotFirstTime() = hasSeenDialog() && getRemindMeLaterDate() > 0

  fun getRemindMeLaterDate() = ratingPreferencesDataSource.getRemindMeLaterDate()

  fun hasSeenDialog() = ratingPreferencesDataSource.hasSeenDialog()

  fun setImpression() = ratingPreferencesDataSource.setImpression()

  fun setRemindMeLater() = ratingPreferencesDataSource.setRemindMeLater()

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