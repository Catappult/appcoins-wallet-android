package com.asfoundation.wallet.home.ui.list

import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction

sealed class HomeListClick {
  data class TransactionClick(val transaction: Transaction) : HomeListClick()
  data class EmptyStateClick(val id: String) : HomeListClick()
  data class NotificationClick(val cardNotification: CardNotification,
                               val cardNotificationAction: CardNotificationAction) : HomeListClick()

  data class ChangedBalanceVisibility(val balanceVisible: Boolean) : HomeListClick()

  object BalanceClick : HomeListClick()
  object ChangeCurrencyClick : HomeListClick()
}