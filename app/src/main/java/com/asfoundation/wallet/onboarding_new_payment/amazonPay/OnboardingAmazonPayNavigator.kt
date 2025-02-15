package com.asfoundation.wallet.onboarding_new_payment.amazonPay

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.asf.wallet.R
import com.asfoundation.wallet.onboarding.pending_payment.OnboardingPaymentFragment
import javax.inject.Inject

class OnboardingAmazonPayNavigator @Inject constructor(
  private val fragment: Fragment,
  private val packageManager: PackageManager
) :
  Navigator {

  fun navigateBack() {
    fragment.findNavController()
      .popBackStack(R.id.onboarding_payment_methods_fragment, inclusive = false)
  }

  fun navigateBackToGame(packageName: String) {
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