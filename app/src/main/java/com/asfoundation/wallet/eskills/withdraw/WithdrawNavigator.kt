package com.asfoundation.wallet.eskills.withdraw

import androidx.fragment.app.FragmentActivity

class WithdrawNavigator(val activity: FragmentActivity) {
  fun navigateBack() = activity.finish()
}
