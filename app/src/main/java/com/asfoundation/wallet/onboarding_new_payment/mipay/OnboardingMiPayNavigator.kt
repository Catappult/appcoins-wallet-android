package com.asfoundation.wallet.onboarding_new_payment.mipay

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.WebViewActivity
import javax.inject.Inject

class OnboardingMiPayNavigator @Inject constructor(
  private val fragment: Fragment,
) :
  Navigator {

  fun navigateBack() {
    fragment.findNavController()
      .popBackStack(R.id.onboarding_payment_methods_fragment, inclusive = false)
  }

  fun navigateToWebView(url: String, webViewLauncher: ActivityResultLauncher<Intent>) {
    webViewLauncher.launch(WebViewActivity.newIntent(fragment.requireActivity(), url))
  }


  fun navigateBackToPaymentMethods() {
    fragment.findNavController()
      .popBackStack(R.id.onboarding_payment_methods_fragment, inclusive = false)
  }
}