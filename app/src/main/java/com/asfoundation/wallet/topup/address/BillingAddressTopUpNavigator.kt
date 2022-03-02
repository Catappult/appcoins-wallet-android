package com.asfoundation.wallet.topup.address

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.asfoundation.wallet.topup.TopUpActivityView
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class BillingAddressTopUpNavigator @Inject constructor(private val fragmentManager: FragmentManager,
                                   private val fragment: Fragment) {

  fun navigateBack() {
    if (fragmentManager.backStackEntryCount != 0) {
      fragmentManager.popBackStack()
    } else {
      (fragment.context as TopUpActivityView).close()
    }
  }
}