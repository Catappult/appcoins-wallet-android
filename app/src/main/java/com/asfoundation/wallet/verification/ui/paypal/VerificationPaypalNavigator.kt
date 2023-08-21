package com.asfoundation.wallet.verification.ui.paypal

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.appcoins.wallet.core.arch.data.Navigator
import com.asfoundation.wallet.ui.iab.WebViewActivity
import javax.inject.Inject

class VerificationPaypalNavigator @Inject constructor(private val fragment: Fragment) :
  Navigator {

  fun navigateToPayment(url: String, launcher: ActivityResultLauncher<Intent>) {
    launcher.launch(WebViewActivity.newIntent(fragment.activity, url))
  }

  fun navigateBack() {
    fragment.activity?.finish()
  }

}