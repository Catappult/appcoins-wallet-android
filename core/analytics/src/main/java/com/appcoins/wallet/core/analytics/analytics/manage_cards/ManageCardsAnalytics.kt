package com.appcoins.wallet.core.analytics.analytics.manage_cards

import cm.aptoide.analytics.AnalyticsManager
import javax.inject.Inject

class ManageCardsAnalytics @Inject constructor(private val analyticsManager: AnalyticsManager) {

  fun openNewCardDetailsPageEvent() {
    analyticsManager.logEvent(
      HashMap<String, Any>(), WALLET_APP_ADD_NEW_CARD_DETAILS_IMPRESSION,
      AnalyticsManager.Action.OPEN, MANAGE_PAYMENT_CARDS
    )
  }

  fun addNewCardDetailsClickEvent() {
    analyticsManager.logEvent(
      HashMap<String, Any>(), WALLET_APP_ADD_NEW_CARD_DETAILS_CLICK,
      AnalyticsManager.Action.CLICK, MANAGE_PAYMENT_CARDS
    )
  }

  fun addedNewCardSuccessEvent() {
    analyticsManager.logEvent(
      HashMap<String, Any>(), WALLET_APP_ADDED_CARD_CONCLUSION_IMPRESSION,
      AnalyticsManager.Action.CLICK, MANAGE_PAYMENT_CARDS
    )
  }

  companion object {
    const val WALLET_APP_ADD_NEW_CARD_DETAILS_IMPRESSION =
      "wallet_app_add_new_card_details_impression"
    private const val WALLET_APP_ADD_NEW_CARD_DETAILS_CLICK =
      "wallet_app_add_new_card_details_click"
    private const val WALLET_APP_ADDED_CARD_CONCLUSION_IMPRESSION =
      "wallet_app_added_card_conclusion_impression"
    private const val WALLET_APP_ADDED_CARD_CONCLUSION_CLICK =
      "wallet_app_added_card_conclusion_click"
    private const val MANAGE_PAYMENT_CARDS = "manage_payment_cards"
  }
}
