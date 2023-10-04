package com.asfoundation.wallet.onboarding_new_payment.payment_methods

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate
import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.entity.TransactionBuilder
import javax.inject.Inject

class OnboardingPaymentMethodsNavigator @Inject constructor(
  private val fragment: Fragment,
  private val packageManager: PackageManager
) :
  Navigator {

  fun navigateToBrowser(uri: Uri) {
    try {
      val launchBrowser = Intent(Intent.ACTION_VIEW, uri)
      launchBrowser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      fragment.requireContext()
        .startActivity(launchBrowser)
    } catch (exception: ActivityNotFoundException) {
      exception.printStackTrace()
      Toast.makeText(fragment.requireContext(), R.string.unknown_error, Toast.LENGTH_SHORT)
        .show()
    }
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

  fun navigateToAdyen(
    transactionBuilder: TransactionBuilder,
    amount: String,
    currency: String,
    forecastBonus: ForecastBonusAndLevel
  ) {
    navigate(
      fragment.findNavController(),
      OnboardingPaymentMethodsFragmentDirections.actionNavigateToOnboardingAdyenPayment(
        transactionBuilder,
        PaymentType.CARD,
        amount,
        currency,
        forecastBonus
      )
    )
  }

  fun navigateToPaypalAdyen(
    transactionBuilder: TransactionBuilder,
    amount: String,
    currency: String,
    forecastBonus: ForecastBonusAndLevel
  ) {
    navigate(
      fragment.findNavController(),
      OnboardingPaymentMethodsFragmentDirections.actionNavigateToOnboardingAdyenPayment(
        transactionBuilder,
        PaymentType.PAYPAL,
        amount,
        currency,
        forecastBonus
      )
    )
  }

  fun navigateToGiroAdyen(
    transactionBuilder: TransactionBuilder,
    amount: String,
    currency: String,
    forecastBonus: ForecastBonusAndLevel
  ) {
    navigate(
      fragment.findNavController(),
      OnboardingPaymentMethodsFragmentDirections.actionNavigateToOnboardingAdyenPayment(
        transactionBuilder,
        PaymentType.GIROPAY,
        amount,
        currency,
        forecastBonus
      )
    )
  }

  fun navigateToLocalPayment(transactionBuilder: TransactionBuilder, paymentId: String, amount: String,
                             currency: String
  ) {
    navigate(
      fragment.findNavController(),
      OnboardingPaymentMethodsFragmentDirections.actionNavigateToOnboardingLocalPayment(
        transactionBuilder,
        paymentId,
        amount,
        currency
      )
    )
  }

  fun navigateToCarrierBilling() = Unit

  fun navigateToShareLinkPayment() = Unit
}