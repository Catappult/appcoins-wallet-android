package com.asfoundation.wallet.onboarding_new_payment.payment_methods

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asf.wallet.R
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.entity.TransactionBuilder
import javax.inject.Inject

class OnboardingPaymentMethodsNavigator @Inject constructor(private val fragment: Fragment) :
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

  fun navigateToAdyen(transactionBuilder: TransactionBuilder, amount: String, currency: String) {
    navigate(
      fragment.findNavController(),
      OnboardingPaymentMethodsFragmentDirections.actionNavigateToOnboardingAdyenPayment(
        transactionBuilder,
        PaymentType.CARD,
        amount,
        currency
      )
    )
  }

  fun navigateToPaypalAdyen(
    transactionBuilder: TransactionBuilder,
    amount: String,
    currency: String
  ) {
    navigate(
      fragment.findNavController(),
      OnboardingPaymentMethodsFragmentDirections.actionNavigateToOnboardingAdyenPayment(
        transactionBuilder,
        PaymentType.PAYPAL,
        amount,
        currency
      )
    )
  }

  fun navigateToPaypalDirect() = Unit

  fun navigateToLocalPayment() = Unit

  fun navigateToCarrierBilling() = Unit

  fun navigateToShareLinkPayment() = Unit
}