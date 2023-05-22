package com.asfoundation.wallet.onboarding_new_payment.use_cases

import com.asfoundation.wallet.ui.iab.PaymentMethod
import javax.inject.Inject

/**
 * This use case should be removed after all payment methods are ready
 * and @GetFirstPaymentMethodsUseCase only should be used instead
 */
class GetOtherPaymentMethodsUseCase @Inject constructor() {

  companion object {
    private const val CARRIE_BILLING = "onebip"
  }

  operator fun invoke(paymentList: List<PaymentMethod>): List<PaymentMethod> {
    val otherPaymentMethods: MutableList<PaymentMethod> = mutableListOf()
    for (method in paymentList) {
      if (method.id == CARRIE_BILLING) {
        otherPaymentMethods.add(method)
      }
    }
    return otherPaymentMethods
  }
}