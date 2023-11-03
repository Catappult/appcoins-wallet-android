package com.asfoundation.wallet.billing.sandbox

import androidx.fragment.app.Fragment
import javax.inject.Inject


class SandboxNavigator @Inject constructor(val fragment: Fragment) {
  fun navigateBack() {
    fragment.requireActivity().onBackPressed()
  }
}
