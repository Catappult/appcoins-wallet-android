package com.asfoundation.wallet.navigator

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.promotions.PromotionsActivity
import com.asfoundation.wallet.router.*
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.ui.WalletsActivity

class TransactionViewNavigator(private val settingsRouter: SettingsRouter,
                               private val sendRouter: SendRouter,
                               private val transactionDetailRouter: TransactionDetailRouter,
                               private val myAddressRouter: MyAddressRouter,
                               private val balanceRouter: BalanceRouter,
                               private val externalBrowserRouter: ExternalBrowserRouter,
                               private val topUpRouter: TopUpRouter,
                               private val updateNavigator: UpdateNavigator) {
  fun openSettings(context: Context) {
    settingsRouter.open(context)
  }

  fun openSendView(context: Context) {
    sendRouter.open(context)
  }

  fun openTransactionsDetailView(context: Context, transaction: Transaction) {
    transactionDetailRouter.open(context, transaction)
  }

  fun openMyAddressView(context: Context, value: Wallet?) {
    myAddressRouter.open(context, value)
  }

  fun openTokensView(context: Context, value: Wallet?) {
    balanceRouter.open(context, value)
  }

  fun navigateToBrowser(context: Context, uri: Uri) {
    externalBrowserRouter.open(context, uri)
  }

  fun openTopUp(context: Context) {
    topUpRouter.open(context)
  }

  fun openPromotions(context: Context) {
    val intent = Intent(context, PromotionsActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    context.startActivity(intent)
  }

  fun openUpdateAppView(context: Context, url: String) {
    updateNavigator.navigateToStoreAppView(context, url)
  }

  fun navigateToBackup(context: Context) {
    val intent = Intent(context, WalletsActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    context.startActivity(intent)
  }

}


