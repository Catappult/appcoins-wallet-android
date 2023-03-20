package com.asfoundation.wallet.rating

import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.core.network.zendesk.RetrofitZendeskNetwork
import com.asf.wallet.BuildConfig
import com.appcoins.wallet.core.network.zendesk.model.WalletFeedbackBody
import io.reactivex.Single
import com.appcoins.wallet.sharedpreferences.RatingPreferencesDataSource
import javax.inject.Inject

class RatingRepository @Inject constructor(
  private val ratingPreferencesDataSource: RatingPreferencesDataSource,
  private val retrofitZendeskNetworkApi: RetrofitZendeskNetwork.RetrofitZendeskNetworkApi,
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
    val body = WalletFeedbackBody(
      WalletFeedbackBody.Ticket(
        "Wallet Feedback - $walletAddress",
        WalletFeedbackBody.Comment(feedbackText)
      )
    )
    return retrofitZendeskNetworkApi.sendFeedback(
      "Basic ${BuildConfig.FEEDBACK_ZENDESK_API_KEY}",
      body
    )
      .map { response -> response.code() == 200 || response.code() == 204 }
      .onErrorReturn { e ->
        logger.log("RatingRepository", e)
        false
      }
  }
}