package com.asfoundation.wallet.ui.transactions.models.cardnotifications

import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyModelGroup
import com.asf.wallet.R
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.ui.transactions.models.DefaultCarouselModel_
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction

class CardNotificationListModel(val data: List<CardNotification>,
                                val listener: ((CardNotification, CardNotificationAction) -> Unit)?) :
    EpoxyModelGroup(
        R.layout.item_card_notifications_list, buildModels(data, listener)) {

  companion object {
    fun buildModels(data: List<CardNotification>,
                    listener: ((CardNotification, CardNotificationAction) -> Unit)?): List<EpoxyModel<*>> {

      val appModels = mutableListOf<CardNotificationModel_>()
      for (notification in data) {
        appModels.add(
            CardNotificationModel_()
                .id("${notification.title} ${notification.body}")
                .cardNotification(notification)
                .clickListener(listener)
        )
      }
      return listOf(
          DefaultCarouselModel_()
              .id("card_notification_list")
              .numViewsToShowOnScreen(1.05f) // This should be consistent on every screen
              .models(appModels)
      )
    }
  }
}