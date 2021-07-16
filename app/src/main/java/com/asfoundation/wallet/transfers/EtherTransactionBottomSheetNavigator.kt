package com.asfoundation.wallet.transfers

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.FragmentManager
import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.main.MainActivityNavigator

class EtherTransactionBottomSheetNavigator(val fragmentManager: FragmentManager,
                                           val fragment: EtherTransactionBottomSheetFragment,
                                           val mainActivityNavigator: MainActivityNavigator,
                                           val networkInfo: NetworkInfo) {

  private fun openUrl(url: String) {
    CustomTabsIntent.Builder()
        .addDefaultShareMenuItem()
        .build()
        .launchUrl(fragment.requireContext(), Uri.parse(url))
  }

  fun navigateToEtherScanTransaction(transactionHash: String) {
    openUrl(networkInfo.etherscanUrl.plus(transactionHash))
    fragmentManager.popBackStack()
  }

  fun goBackToTransactions() {
    mainActivityNavigator.navigateToHome()
  }
}