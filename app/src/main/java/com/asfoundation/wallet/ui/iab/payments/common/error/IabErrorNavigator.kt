package com.asfoundation.wallet.ui.iab.payments.common.error

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import com.asfoundation.wallet.ui.iab.IabActivity
import javax.inject.Inject

class IabErrorNavigator @Inject constructor(fragment: Fragment,
                        private val fragmentManager: FragmentManager) {

  private val iabActivity = fragment.activity as IabActivity

  fun cancelPayment() = iabActivity.finishWithError()

  fun navigateBackToPayment(backStackEntryName: String) =
      fragmentManager.popBackStack(backStackEntryName, POP_BACK_STACK_INCLUSIVE)

}