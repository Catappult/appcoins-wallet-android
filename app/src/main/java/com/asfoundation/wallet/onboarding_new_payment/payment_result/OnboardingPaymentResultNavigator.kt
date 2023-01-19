package com.asfoundation.wallet.onboarding_new_payment.payment_result

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.base.Navigator
import javax.inject.Inject

class OnboardingPaymentResultNavigator @Inject constructor(private val fragment: Fragment) :
  Navigator {

  fun navigateBack() {
    fragment.findNavController().popBackStack()
  }
}