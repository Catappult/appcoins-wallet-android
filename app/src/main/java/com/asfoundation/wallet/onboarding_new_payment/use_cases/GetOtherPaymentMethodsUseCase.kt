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
    private const val ASK_SOMEONE_TO_PAY_ID = "ask_friend"
  }

  operator fun invoke(paymentList: List<PaymentMethod>): List<PaymentMethod> {
    val otherPaymentMethods: MutableList<PaymentMethod> = mutableListOf()
    for (method in paymentList) {
      if (method.id == ASK_SOMEONE_TO_PAY_ID) {
        otherPaymentMethods.add(method)
      }
    }
    return otherPaymentMethods
  }
}