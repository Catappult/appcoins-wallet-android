package com.appcoins.wallet.core.analytics.analytics.rewards

import cm.aptoide.analytics.AnalyticsManager
import javax.inject.Inject

class RewardsAnalytics @Inject constructor(private val analyticsManager: AnalyticsManager) {

  fun rewardsImpressionEvent() {
    analyticsManager.logEvent(
      HashMap<String, Any>(),
      WALLET_APP_REWARDS_SCREEN_IMPRESSION,
      AnalyticsManager.Action.IMPRESSION,
      REWARDS
    )
  }

  fun promoCodeClickEvent() {
    val data = HashMap<String, Any>()
    data[CLICK_ACTION] = ADD_PROMO_CODE
    analyticsManager.logEvent(
      data,
      WALLET_APP_REWARDS_SCREEN_CLICK,
      AnalyticsManager.Action.CLICK,
      REWARDS
    )
  }

  fun newPromoCodeImpressionEvent() {
    analyticsManager.logEvent(
      HashMap<String, Any>(),
      WALLET_APP_SUBMIT_NEW_PROMO_CODE_IMPRESSION,
      AnalyticsManager.Action.IMPRESSION,
      REWARDS
    )
  }

  fun submitNewPromoCodeClickEvent(promoCode: String) {
    val data = HashMap<String, Any>()
    data[CLICK_ACTION] = SUBMIT
    data[PROMO_CODE] = promoCode
    analyticsManager.logEvent(
      data,
      WALLET_APP_SUBMIT_NEW_PROMO_CODE_CLICK,
      AnalyticsManager.Action.CLICK,
      REWARDS
    )
  }

  fun promoCodeSuccessImpressionEvent(promoCode: String) {
    val data = HashMap<String, Any>()
    data[PROMO_CODE] = promoCode
    analyticsManager.logEvent(
      data,
      WALLET_APP_SUBMIT_PROMO_CODE_SUCCESS_IMPRESSION,
      AnalyticsManager.Action.IMPRESSION,
      REWARDS
    )
  }

  fun promoCodeSuccessGotItClickEvent(promoCode: String) {
    val data = HashMap<String, Any>()
    data[CLICK_ACTION] = GOT_IT
    data[PROMO_CODE] = promoCode
    analyticsManager.logEvent(
      data,
      WALLET_APP_SUBMIT_PROMO_CODE_SUCCESS_CLICK,
      AnalyticsManager.Action.CLICK,
      REWARDS
    )
  }

  fun promoCodeErrorImpressionEvent(promoCode: String) {
    val data = HashMap<String, Any>()
    data[PROMO_CODE] = promoCode
    analyticsManager.logEvent(
      data,
      WALLET_APP_SUBMIT_PROMO_CODE_ERROR_IMPRESSION,
      AnalyticsManager.Action.IMPRESSION,
      REWARDS
    )
  }

  fun promoCodeErrorTryAgainClickEvent(promoCode: String) {
    val data = HashMap<String, Any>()
    data[CLICK_ACTION] = TRY_AGAIN
    data[PROMO_CODE] = promoCode
    analyticsManager.logEvent(
      data,
      WALLET_APP_SUBMIT_PROMO_CODE_ERROR_CLICK,
      AnalyticsManager.Action.CLICK,
      REWARDS
    )
  }

  fun replacePromoCodeImpressionEvent(promoCode: String) {
    val data = HashMap<String, Any>()
    data[PROMO_CODE] = promoCode
    analyticsManager.logEvent(
      data,
      WALLET_APP_REPLACE_PROMO_CODE_IMPRESSION,
      AnalyticsManager.Action.IMPRESSION,
      REWARDS
    )
  }

  fun replacePromoCodeClickEvent(promoCode: String) {
    val data = HashMap<String, Any>()
    data[CLICK_ACTION] = REPLACE
    data[PROMO_CODE] = promoCode
    analyticsManager.logEvent(
      data,
      WALLET_APP_REPLACE_PROMO_CODE_CLICK,
      AnalyticsManager.Action.CLICK,
      REWARDS
    )
  }

  companion object {
    private const val WALLET_APP_REWARDS_SCREEN_IMPRESSION = "wallet_app_rewards_screen_impression"
    private const val WALLET_APP_REWARDS_SCREEN_CLICK = "wallet_app_rewards_screen_click"
    private const val WALLET_APP_SUBMIT_NEW_PROMO_CODE_IMPRESSION =
      "wallet_app_submit_new_promo_code_impression"
    private const val WALLET_APP_SUBMIT_NEW_PROMO_CODE_CLICK =
      "wallet_app_submit_new_promo_code_click"
    private const val WALLET_APP_SUBMIT_PROMO_CODE_SUCCESS_IMPRESSION =
      "wallet_app_submit_promo_code_success_impression"
    private const val WALLET_APP_SUBMIT_PROMO_CODE_SUCCESS_CLICK =
      "wallet_app_submit_promo_code_success_click"
    private const val WALLET_APP_SUBMIT_PROMO_CODE_ERROR_IMPRESSION =
      "wallet_app_submit_promo_code_error_impression"
    private const val WALLET_APP_SUBMIT_PROMO_CODE_ERROR_CLICK =
      "wallet_app_submit_promo_code_error_click"
    private const val WALLET_APP_REPLACE_PROMO_CODE_IMPRESSION =
      "wallet_app_replace_promo_code_impression"
    private const val WALLET_APP_REPLACE_PROMO_CODE_CLICK = "wallet_app_replace_promo_code_click"

    private const val REWARDS = "rewards"
    private const val PROMO_CODE = "promo_code"
    private const val CLICK_ACTION = "click_action"

    private const val ADD_PROMO_CODE = "add_promo_code"
    private const val SUBMIT = "submit"
    private const val TRY_AGAIN = "try_again"
    private const val GOT_IT = "got_it"
    private const val DELETE = "delete"
    private const val REPLACE = "replace"


  }
}
