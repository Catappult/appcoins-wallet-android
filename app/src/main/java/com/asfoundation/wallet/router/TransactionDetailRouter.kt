package com.asfoundation.wallet.router

import android.content.Context
import android.content.Intent
import com.appcoins.wallet.core.utils.jvm_common.C
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.ui.balance.TransactionDetailActivity
import javax.inject.Inject

class TransactionDetailRouter @Inject constructor() {
  fun open(context: Context, transaction: Transaction, globalBalanceCurrency: String) {
    with(context) {
      val intent = Intent(this, TransactionDetailActivity::class.java)
          .apply {
            putExtra(C.Key.TRANSACTION, transaction)
            putExtra(C.Key.GLOBAL_BALANCE_CURRENCY, globalBalanceCurrency)
          }
      startActivity(intent)
    }
  }
}