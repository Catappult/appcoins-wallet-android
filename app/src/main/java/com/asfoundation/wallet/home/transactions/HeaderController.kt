package com.asfoundation.wallet.home.transactions

import com.airbnb.epoxy.EpoxyAsyncUtil
import com.airbnb.epoxy.TypedEpoxyController
import com.asfoundation.wallet.home.transactions.models.cardnotifications.CardNotificationListModel
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.ui.widget.entity.TransactionsModel
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction

class HeaderController :
    TypedEpoxyController<TransactionsModel>(EpoxyAsyncUtil.getAsyncBackgroundHandler(),
        EpoxyAsyncUtil.getAsyncBackgroundHandler()) {

  var cardNotificationClickListener: ((CardNotification, CardNotificationAction) -> Unit)? = null

  override fun buildModels(txModel: TransactionsModel) {
    val notifications: List<CardNotification> = txModel.notifications
    if (notifications.isNotEmpty()) {
      add(CardNotificationListModel(notifications, cardNotificationClickListener))
    }
  }
}