package com.asfoundation.wallet.rating

import android.content.SharedPreferences
import com.asf.wallet.BuildConfig
import com.appcoins.wallet.commons.Logger
import com.asfoundation.wallet.rating.network.WalletFeedbackBody
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

class RatingRepository(private val sharedPreferences: SharedPreferences,
                       private val walletFeedbackApi: WalletFeedbackApi,
                       private val logger: Logger) {

  companion object {
    const val REMIND_ME_LATER_TIME_KEY = "first_time_rating"
    const val IMPRESSION_KEY = "impression_rating"
    const val HAS_ENOUGH_SUCCESSFUL_TRANSACTIONS_ = "has_enough_successful_transactions"
    private const val MONTH = 30L * 24 * 60 * 60 * 1000
  }

  fun saveEnoughSuccessfulTransactions() {
    sharedPreferences.edit()
        .putBoolean(HAS_ENOUGH_SUCCESSFUL_TRANSACTIONS_, true)
        .apply()
  }

  fun hasEnoughSuccessfulTransactions(): Boolean {
    return sharedPreferences.getBoolean(HAS_ENOUGH_SUCCESSFUL_TRANSACTIONS_, false)
  }

  /**
   * This method is similar to hasSeenDialog but it differs in use:
   *  - hasSeenDialog should return true as soon as the entry fragment is displayed for the first time
   *  - This methood should only return true once the rating flow is seen and closed for the first time
   */
  fun isNotFirstTime(): Boolean {
    return hasSeenDialog() && sharedPreferences.getLong(REMIND_ME_LATER_TIME_KEY, -1L) > 0
  }

  fun getRemindMeLaterDate(): Long {
    return sharedPreferences.getLong(REMIND_ME_LATER_TIME_KEY, -1L)
  }

  fun hasSeenDialog(): Boolean {
    return sharedPreferences.getBoolean(IMPRESSION_KEY, false)
  }

  fun setImpression() {
    sharedPreferences.edit()
        .putBoolean(IMPRESSION_KEY, true)
        .apply()
    sharedPreferences.edit()
        .putLong(REMIND_ME_LATER_TIME_KEY, -1L)
        .apply()
  }

  fun setRemindMeLater() {
    sharedPreferences.edit()
        .putLong(REMIND_ME_LATER_TIME_KEY, System.currentTimeMillis() + MONTH)
        .apply()
  }

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