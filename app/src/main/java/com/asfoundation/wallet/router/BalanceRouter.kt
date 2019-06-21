package com.asfoundation.wallet.router

import android.content.Context
import android.content.Intent
import com.asfoundation.wallet.C.Key.WALLET
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.ui.balance.BalanceActivity

class BalanceRouter {

  fun open(context: Context, wallet: Wallet?) {
    wallet?.let {
      val intent = Intent(context, BalanceActivity::class.java)
      intent.putExtra(WALLET, wallet)
      intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
      context.startActivity(intent)
    }
  }
}
