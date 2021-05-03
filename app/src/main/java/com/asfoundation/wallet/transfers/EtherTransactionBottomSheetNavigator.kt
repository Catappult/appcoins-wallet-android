package com.asfoundation.wallet.transfers

import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.FragmentManager
import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.ui.TransactionsActivity

class EtherTransactionBottomSheetNavigator(val fragmentManager: FragmentManager,
                                           val fragment: EtherTransactionBottomSheetFragment,
                                           val networkInfo: NetworkInfo) {

  private fun openUrl(url: String) {
    CustomTabsIntent.Builder()
        .addDefaultShareMenuItem()
        .build()
        .launchUrl(fragment.context!!, Uri.parse(url))
  }

  fun navigateToEtherScanTransaction(transactionHash: String) {
    openUrl(networkInfo.etherscanUrl.plus(transactionHash))
    fragmentManager.popBackStack()
  }

  fun goBackToTransactions() {
    val intent: Intent = TransactionsActivity.newIntent(fragment.context)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    fragment.startActivity(intent)
  }
}