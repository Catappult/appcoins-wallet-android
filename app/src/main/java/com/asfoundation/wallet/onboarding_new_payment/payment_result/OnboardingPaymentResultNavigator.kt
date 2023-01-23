package com.asfoundation.wallet.onboarding_new_payment.payment_result

import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.asf.wallet.R
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.main.PendingIntentNavigator
import com.asfoundation.wallet.onboarding_new_payment.payment_methods.OnboardingPaymentMethodsFragmentDirections
import javax.inject.Inject

class OnboardingPaymentResultNavigator @Inject constructor(
  private val fragment: Fragment,
  private val packageManager: PackageManager,
  private val pendingIntentNavigator: PendingIntentNavigator
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
    pendingIntentNavigator.getHomePendingIntent().send()
  }

  fun navigateBackToPaymentMethods() {
    fragment.findNavController()
      .popBackStack(R.id.onboarding_payment_methods_fragment, inclusive = false)
  }
}