package com.asfoundation.wallet.transfers

import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.FragmentManager
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.ui.TransactionsActivity

class EtherTransactionBottomSheetNavigator(val fragmentManager: FragmentManager,
                                           val fragment: EtherTransactionBottomSheetFragment) {

  private fun setupBaseUrl(transactionHash: String): String {
    return if (BuildConfig.DEBUG) {
      "https://ropsten.etherscan.io/tx/$transactionHash"
    } else {
      "https://etherscan.io/tx/$transactionHash"
    }
  }

  private fun openUrl(url: String) {
    CustomTabsIntent.Builder()
        .addDefaultShareMenuItem()
        .build()
        .launchUrl(fragment.context!!, Uri.parse(url))
  }

  fun navigateToEtherScanTransaction(transactionHash: String) {
    openUrl(setupBaseUrl(transactionHash))
    fragmentManager.popBackStack()
  }

  fun goBackToTransactions() {
    val intent: Intent = TransactionsActivity.newIntent(fragment.context)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    fragment.startActivity(intent)
  }
}