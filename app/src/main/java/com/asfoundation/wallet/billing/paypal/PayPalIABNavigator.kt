package com.asfoundation.wallet.billing.paypal

import androidx.fragment.app.Fragment
import javax.inject.Inject


class PayPalIABNavigator @Inject constructor(val fragment: Fragment) {
  fun navigateBack() {
    fragment.requireActivity().onBackPressed()
  }
}
