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

  fun removeCardClickEvent() {
    analyticsManager.logEvent(
      HashMap<String, Any>(), WALLET_APP_REMOVE_SAVED_CARD_PROMPT_CLICK,
      AnalyticsManager.Action.CLICK, MANAGE_PAYMENT_CARDS
    )
  }

  fun removeCardSuccessEvent() {
    analyticsManager.logEvent(
      HashMap<String, Any>(), WALLET_APP_REMOVED_CARD_CONCLUSION_IMPRESSION,
      AnalyticsManager.Action.CLICK, MANAGE_PAYMENT_CARDS
    )
  }

  fun managePaymentCardsImpression() {
    analyticsManager.logEvent(
      HashMap<String, Any>(), WALLET_APP_MANAGE_PAYMENT_CARDS_IMPRESSION,
      AnalyticsManager.Action.OPEN, MANAGE_PAYMENT_CARDS
    )
  }

  fun settingsManageCardsClickEvent() {
    analyticsManager.logEvent(
      HashMap<String, Any>(), WALLET_APP_SETTINGS_CLICK,
      AnalyticsManager.Action.CLICK, MANAGE_PAYMENT_CARDS
    )
  }

  companion object {
    const val WALLET_APP_ADD_NEW_CARD_DETAILS_IMPRESSION =
      "wallet_app_add_new_card_details_impression"
    const val WALLET_APP_ADD_NEW_CARD_DETAILS_CLICK =
      "wallet_app_add_new_card_details_click"
    const val WALLET_APP_ADDED_CARD_CONCLUSION_IMPRESSION =
      "wallet_app_added_card_conclusion_impression"
    const val MANAGE_PAYMENT_CARDS = "manage_payment_cards"
    const val WALLET_APP_REMOVE_SAVED_CARD_PROMPT_CLICK =
      "wallet_app_remove_saved_card_prompt_click"
    const val WALLET_APP_REMOVED_CARD_CONCLUSION_IMPRESSION =
      "wallet_app_removed_card_conclusion_impression"
    const val WALLET_APP_MANAGE_PAYMENT_CARDS_IMPRESSION =
      "wallet_app_manage_payment_cards_impression"
    const val WALLET_APP_SETTINGS_CLICK = "wallet_app_settings_click"


  }
}
