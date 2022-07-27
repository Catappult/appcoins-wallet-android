package com.asfoundation.wallet.transfers

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.main.nav_bar.NavBarFragmentNavigator
import javax.inject.Inject

class EtherTransactionBottomSheetNavigator @Inject constructor(
  val fragmentManager: FragmentManager,
  val fragment: Fragment,
  val networkInfo: NetworkInfo,
  val navBarFragmentNavigator: NavBarFragmentNavigator
) {

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
//    navBarFragmentNavigator.navigateToHome()
    fragment.activity?.finish()
  }
}