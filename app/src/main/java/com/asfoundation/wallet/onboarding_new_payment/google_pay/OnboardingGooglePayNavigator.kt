package com.asfoundation.wallet.onboarding_new_payment.google_pay

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate
import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.onboarding.pending_payment.OnboardingPaymentFragment
import javax.inject.Inject

class OnboardingGooglePayNavigator
@Inject
constructor(private val fragment: Fragment, private val packageManager: PackageManager) :
    Navigator {

  fun navigateBack() {
    fragment.findNavController().popBackStack()
  }

  /**
   * passed navController should be used to be able to replicate the design, but
   * fragment.findNavController() is a temporary solution to navigate, even though its to the wrong
   * navController see:
   * https://stackoverflow.com/questions/50730494/new-navigation-component-from-arch-with-nested-navigation-graph
   * see:
   * https://stackoverflow.com/questions/74717334/android-nested-fragment-navigation?noredirect=1#comment132543414_74717334
   */
  fun navigateToPaymentResult(
      transactionBuilder: TransactionBuilder,
      paymentType: PaymentType,
      amount: String,
      currency: String,
      forecastBonus: ForecastBonusAndLevel
  ) {
    navigate(
        fragment.findNavController(),
        OnboardingGooglePayFragmentDirections.actionNavigateToOnboardingPaymentResult(
            PaymentModel(), transactionBuilder, paymentType, amount, currency, forecastBonus))
  }

  fun navigateBackToGame(packageName: String) {
    try {
      fragment.startActivity(packageManager.getLaunchIntentForPackage(packageName)!!)
    } catch (e: Throwable) {
      e.printStackTrace()
      fragment.activity?.finishAffinity()
    }
  }

  fun navigateToHome() {
    fragment.requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    fragment.setFragmentResult(
        OnboardingPaymentFragment.ONBOARDING_PAYMENT_CONCLUSION,
        bundleOf("fragmentEnded" to "result"))
  }
}
