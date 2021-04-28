package com.asfoundation.wallet.transfers

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

  private fun openUrl(url: String?) {
    val builder = CustomTabsIntent.Builder()
    builder.addDefaultShareMenuItem()
    builder.build()
        .launchUrl(fragment.context!!, Uri.parse(url))
  }

  fun navigateToEtherScanTransaction(transactionHash: String) {
    setupBaseUrl(transactionHash)
    openUrl(setupBaseUrl(transactionHash))
    fragmentManager.popBackStack()
  }

  fun goBackToTransactions() {
    fragment.startActivity(
        TransactionsActivity.newIntent(
            fragment.context!!))
    fragmentManager.popBackStack()
  }
}