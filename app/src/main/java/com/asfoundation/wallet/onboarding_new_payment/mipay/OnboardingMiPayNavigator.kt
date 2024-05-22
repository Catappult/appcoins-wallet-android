package com.asfoundation.wallet.onboarding_new_payment.mipay

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.asf.wallet.R
import com.asfoundation.wallet.onboarding.pending_payment.OnboardingPaymentFragment
import com.asfoundation.wallet.ui.iab.WebViewActivity
import javax.inject.Inject

class OnboardingMiPayNavigator @Inject constructor(
  private val fragment: Fragment,
  private val packageManager: PackageManager
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

  fun navigateBackToTheGame(packageName: String) {
    try {
      fragment.startActivity(
        packageManager.getLaunchIntentForPackage(packageName)!!
      )
    } catch (e: Throwable) {
      e.printStackTrace()
      fragment.activity?.finishAffinity()
    }
  }

  fun navigateToHome() {
    fragment.requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    fragment.setFragmentResult(
      OnboardingPaymentFragment.ONBOARDING_PAYMENT_CONCLUSION,
      bundleOf("fragmentEnded" to "result")
    )
  }
}