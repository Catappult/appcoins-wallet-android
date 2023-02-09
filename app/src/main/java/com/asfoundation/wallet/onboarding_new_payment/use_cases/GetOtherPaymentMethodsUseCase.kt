package com.asfoundation.wallet.onboarding_new_payment.use_cases

import com.asfoundation.wallet.ui.iab.PaymentMethod
import javax.inject.Inject

/**
 * This use case should be removed after all payment methods are ready
 * and @GetFirstPaymentMethodsUseCase only should be used instead
 */
class GetOtherPaymentMethodsUseCase @Inject constructor() {

  companion object {
    private const val PAYPAL_ID = "paypal"
    private const val CC_ID = "credit_card"
  }

  operator fun invoke(paymentList: List<PaymentMethod>): List<PaymentMethod> {
    val otherPaymentMethods: MutableList<PaymentMethod> = mutableListOf()
    for (method in paymentList) {
      if (method.id != CC_ID && method.id != PAYPAL_ID) {
        otherPaymentMethods.add(method)
      }
    }
    return otherPaymentMethods
  }
}