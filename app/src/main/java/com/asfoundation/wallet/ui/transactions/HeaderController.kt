package com.asfoundation.wallet.ui.transactions

import com.airbnb.epoxy.EpoxyAsyncUtil
import com.airbnb.epoxy.TypedEpoxyController
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.ui.appcoins.applications.AppcoinsApplication
import com.asfoundation.wallet.ui.transactions.models.appcoinsapps.AppcoinsAppListModel
import com.asfoundation.wallet.ui.transactions.models.cardnotifications.CardNotificationListModel
import com.asfoundation.wallet.ui.widget.entity.TransactionsModel
import com.asfoundation.wallet.ui.widget.holder.ApplicationClickAction
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction

class HeaderController :
    TypedEpoxyController<TransactionsModel>(EpoxyAsyncUtil.getAsyncBackgroundHandler(),
        EpoxyAsyncUtil.getAsyncBackgroundHandler()) {

  var cardNotificationClickListener: ((CardNotification, CardNotificationAction) -> Unit)? = null
  var appcoinsAppClickListener: ((AppcoinsApplication, ApplicationClickAction) -> Unit)? = null

  override fun buildModels(txModel: TransactionsModel) {
    val notifications: List<CardNotification> = txModel.notifications
    if (notifications.isNotEmpty()) {
      add(CardNotificationListModel(notifications, cardNotificationClickListener))
    } else {
      val appList: List<AppcoinsApplication> = txModel.applications
      if (appList.isNotEmpty())
        add(AppcoinsAppListModel(appList, appcoinsAppClickListener))
    }
  }
}