package com.asfoundation.wallet.eskills.withdraw

import android.app.Activity
import javax.inject.Inject

class WithdrawNavigator @Inject constructor(val activity: Activity) {
  fun navigateBack() = activity.finish()
}
