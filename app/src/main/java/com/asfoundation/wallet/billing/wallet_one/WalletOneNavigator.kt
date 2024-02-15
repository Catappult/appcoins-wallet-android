package com.asfoundation.wallet.billing.wallet_one

import androidx.fragment.app.Fragment
import javax.inject.Inject


class WalletOneNavigator @Inject constructor(val fragment: Fragment) {
  fun navigateBack() {
    fragment.requireActivity().onBackPressed()
  }
}
