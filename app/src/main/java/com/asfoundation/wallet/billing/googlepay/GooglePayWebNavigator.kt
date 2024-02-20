package com.asfoundation.wallet.billing.googlepay

import androidx.fragment.app.Fragment
import javax.inject.Inject

class GooglePayWebNavigator @Inject constructor(val fragment: Fragment) {
  fun navigateBack() {
    fragment.requireActivity().onBackPressed()
  }
}
