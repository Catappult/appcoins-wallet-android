package com.asfoundation.wallet.verification.ui.paypal

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.ui.iab.WebViewActivity

class VerificationPaypalNavigator(private val fragment: VerificationPaypalFragment) :
    Navigator {

  fun navigateToPayment(url: String, launcher: ActivityResultLauncher<Intent>) {
    launcher.launch(WebViewActivity.newIntent(fragment.activity, url))
  }

  fun navigateBack() {
    fragment.activity?.finish()
  }

}