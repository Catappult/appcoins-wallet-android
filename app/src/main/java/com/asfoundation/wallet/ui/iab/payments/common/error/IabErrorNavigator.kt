package com.asfoundation.wallet.ui.iab.payments.common.error

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

class IabErrorNavigator(private val activity: FragmentActivity,
                        private val fragmentManager: FragmentManager) {

  fun navigateToOtherPayments(backStackEntryName: String) {
    fragmentManager.popBackStack(backStackEntryName, FragmentManager.POP_BACK_STACK_INCLUSIVE)
  }

  fun cancelPayment() {
    // This assumes IabActivity is listening for back presses to cancel the payment
    activity.onBackPressed()
  }

  fun navigateBackToPayment(backStackEntryName: String) {
    fragmentManager.popBackStack(backStackEntryName, 0)
  }

}