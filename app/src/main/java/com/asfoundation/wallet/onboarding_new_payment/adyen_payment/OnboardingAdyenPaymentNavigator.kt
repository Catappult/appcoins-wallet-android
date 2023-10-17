package com.asfoundation.wallet.onboarding_new_payment.adyen_payment

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.iab.WebViewActivity
import javax.inject.Inject

class OnboardingAdyenPaymentNavigator @Inject constructor(private val fragment: Fragment) :
  Navigator {

  fun navigateBack() {
    fragment.findNavController().popBackStack()
  }

  fun navigateToWebView(url: String, webViewLauncher: ActivityResultLauncher<Intent>) {
    webViewLauncher.launch(WebViewActivity.newIntent(fragment.requireActivity(), url))
  }

  /**
   * passed navController should be used to be able to replicate the design, but fragment.findNavController()
   * is a temporary solution to navigate, even though its to the wrong navController
   * see: https://stackoverflow.com/questions/50730494/new-navigation-component-from-arch-with-nested-navigation-graph
   * see: https://stackoverflow.com/questions/74717334/android-nested-fragment-navigation?noredirect=1#comment132543414_74717334
   */

  fun navigateToPaymentResult(
    navController: NavController,
    paymentModel: PaymentModel,
    transactionBuilder: TransactionBuilder,
    paymentType: PaymentType,
    amount: String,
    currency: String,
    forecastBonus: ForecastBonusAndLevel
  ) {

    navigate(
      fragment.findNavController(),
      OnboardingAdyenPaymentFragmentDirections.actionNavigateToOnboardingPaymentResult(
        paymentModel,
        transactionBuilder,
        paymentType,
        amount,
        currency,
        forecastBonus
      )
    )
  }
}