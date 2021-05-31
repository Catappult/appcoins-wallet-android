package com.asfoundation.wallet.navigator

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.router.*
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.ui.backup.BackupActivity
import com.asfoundation.wallet.ui.settings.SettingsActivity

class TransactionViewNavigator(private val sendRouter: SendRouter,
                               private val transactionDetailRouter: TransactionDetailRouter,
                               private val myAddressRouter: MyAddressRouter,
                               private val balanceRouter: BalanceRouter,
                               private val externalBrowserRouter: ExternalBrowserRouter,
                               private val topUpRouter: TopUpRouter) {

  fun openSettings(context: Context, turnOnFingerprint: Boolean = false) {
    context.startActivity(SettingsActivity.newIntent(context, turnOnFingerprint))
  }

  fun openSendView(context: Context) = sendRouter.open(context)

  fun openTransactionsDetailView(context: Context, transaction: Transaction,
                                 globalBalanceCurrency: String) =
      transactionDetailRouter.open(context, transaction, globalBalanceCurrency)

  fun openMyAddressView(context: Context, value: Wallet?) = myAddressRouter.open(context, value)

  fun openMyWalletsView(context: Context) = balanceRouter.open(context)

  fun navigateToBrowser(context: Context, uri: Uri) = externalBrowserRouter.open(context, uri)

  fun openIntent(context: Context, intent: Intent) = context.startActivity(intent)

  fun openTopUp(context: Context) = topUpRouter.open(context)

  fun openPromotions(context: Context) {
    // TODO: IMPORTANT! This has to be refactored once home is only a fragment
    // TODO: Use MainActivityNavigator
//    val intent = Intent(context, PromotionsActivity::class.java)
//        .apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP }
//    context.startActivity(intent)
  }

  fun navigateToBackup(context: Context, walletAddress: String) {
    val intent = BackupActivity.newIntent(context, walletAddress)
        .apply {
          flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
    context.startActivity(intent)
  }
}


