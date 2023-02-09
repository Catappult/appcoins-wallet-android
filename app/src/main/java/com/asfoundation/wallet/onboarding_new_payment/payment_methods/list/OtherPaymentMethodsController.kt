package com.asfoundation.wallet.onboarding_new_payment.payment_methods.list

import com.airbnb.epoxy.TypedEpoxyController
import com.asfoundation.wallet.onboarding_new_payment.payment_methods.list.model.OtherPaymentMethodModel_
import com.asfoundation.wallet.ui.iab.PaymentMethod

class OtherPaymentMethodsController : TypedEpoxyController<List<PaymentMethod>>() {

  override fun buildModels(model: List<PaymentMethod>) {
    for (paymentMethod in model) {
      add(
        OtherPaymentMethodModel_()
          .id(paymentMethod.id)
          .paymentMethod(paymentMethod)
      )
    }
  }
}