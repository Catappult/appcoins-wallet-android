package com.asfoundation.wallet.topup.address

import androidx.fragment.app.FragmentManager
import com.asfoundation.wallet.topup.TopUpActivityView

class BillingAddressTopUpNavigator(private val fragmentManager: FragmentManager,
                                   private val topUpActivityView: TopUpActivityView) {

  fun navigateBack() {
    if (fragmentManager.backStackEntryCount != 0) {
      fragmentManager.popBackStack()
    } else {
      topUpActivityView.close()
    }
  }
}