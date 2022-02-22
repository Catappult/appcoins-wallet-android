package com.asfoundation.wallet.home.ui.list.header.notifications

import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyModelGroup
import com.asf.wallet.R
import com.asfoundation.wallet.home.ui.list.HomeListClick
import com.asfoundation.wallet.referrals.CardNotification

class CardNotificationListModel(val data: List<CardNotification>,
                                val listener: ((HomeListClick) -> Unit)?) :
    EpoxyModelGroup(
        R.layout.item_card_notifications_list, buildModels(data, listener)) {

  companion object {
    fun buildModels(data: List<CardNotification>,
                    listener: ((HomeListClick) -> Unit)?): List<EpoxyModel<*>> {

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
          .numViewsToShowOnScreen(if(appModels.size==1) 0.0f else 1.05f ) // This should be consistent on every screen
          .models(appModels)
      )
    }
  }
}