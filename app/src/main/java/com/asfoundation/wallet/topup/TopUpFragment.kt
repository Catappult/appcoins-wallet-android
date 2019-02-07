package com.asfoundation.wallet.topup

import dagger.android.support.DaggerFragment

class TopUpFragment : DaggerFragment() {
  companion object {
    @JvmStatic
    fun newInstance(): TopUpFragment {
      return TopUpFragment()
    }
  }
}
