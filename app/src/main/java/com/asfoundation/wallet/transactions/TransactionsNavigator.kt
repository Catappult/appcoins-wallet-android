package com.asfoundation.wallet.transactions

import android.os.Bundle
import androidx.navigation.NavController
import com.appcoins.wallet.ui.arch.data.Navigator
import com.asf.wallet.R
import com.asfoundation.wallet.transactions.TransactionDetailsFragment.Companion.TRANSACTION_KEY
import javax.inject.Inject

class TransactionsNavigator @Inject constructor() : Navigator {

  fun navigateToTransactionDetails(navController: NavController, transaction: TransactionModel) {
    val bundle = Bundle()
    bundle.putParcelable(TRANSACTION_KEY, transaction)
    navController.navigate(resId = R.id.action_navigate_to_transaction_details, args = bundle)
  }

  fun navigateToTransactionsList(navController: NavController) =
    navController.navigate(R.id.action_navigate_to_transactions_list)
}
