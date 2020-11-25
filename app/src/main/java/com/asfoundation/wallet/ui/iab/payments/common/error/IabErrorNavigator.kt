package com.asfoundation.wallet.ui.iab.payments.common.error

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import com.asfoundation.wallet.ui.iab.IabActivity

class IabErrorNavigator(private val activity: IabActivity,
                        private val fragmentManager: FragmentManager) {

  fun cancelPayment() = activity.finishWithError()

  fun navigateBackToPayment(backStackEntryName: String) =
      fragmentManager.popBackStack(backStackEntryName, POP_BACK_STACK_INCLUSIVE)

}