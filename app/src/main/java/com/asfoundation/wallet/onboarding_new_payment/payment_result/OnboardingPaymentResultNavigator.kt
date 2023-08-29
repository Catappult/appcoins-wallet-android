package com.asfoundation.wallet.onboarding_new_payment.payment_result

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.asf.wallet.R
import com.appcoins.wallet.core.arch.data.Navigator
import com.asfoundation.wallet.onboarding.pending_payment.OnboardingPaymentFragment.Companion.ONBOARDING_PAYMENT_CONCLUSION
import com.asfoundation.wallet.verification.ui.credit_card.VerificationCreditCardActivity
import javax.inject.Inject

class OnboardingPaymentResultNavigator @Inject constructor(
  private val fragment: Fragment,
  private val packageManager: PackageManager
) :
  Navigator {

  fun navigateBack() {
    fragment.findNavController().popBackStack()
  }

  fun navigateBackToGame(packageName: String) {
    try {
      fragment.startActivity(
        packageManager.getLaunchIntentForPackage(packageName)
      )
    } catch (e: Throwable) {
      e.printStackTrace()
      fragment.activity?.finishAffinity()
    }
  }

  fun navigateToHome() {
    fragment.requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    fragment.setFragmentResult(ONBOARDING_PAYMENT_CONCLUSION, bundleOf("fragmentEnded" to "result"))
  }

  fun navigateBackToPaymentMethods() {
    fragment.requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    fragment.findNavController()
      .popBackStack(R.id.onboarding_payment_methods_fragment, inclusive = false)
  }

  fun navigateToVerifyActivity(walletVerified: Boolean) {
    val intent = VerificationCreditCardActivity.newIntent(fragment.requireContext(), walletVerified)
      .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
    fragment.requireContext().startActivity(intent)
  }
}