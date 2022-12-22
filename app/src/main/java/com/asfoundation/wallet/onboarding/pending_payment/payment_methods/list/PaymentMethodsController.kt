package com.asfoundation.wallet.onboarding.pending_payment.payment_methods.list

import com.airbnb.epoxy.TypedEpoxyController
import com.asfoundation.wallet.onboarding.pending_payment.payment_methods.list.model.PaymentMethodModel_
import com.asfoundation.wallet.ui.iab.PaymentMethod

class PaymentMethodsController : TypedEpoxyController<List<PaymentMethod>>() {

  var clickListener: ((PaymentMethod) -> Unit)? = null

  override fun buildModels(model: List<PaymentMethod>) {
    for (paymentMethod in model) {
      add(
        PaymentMethodModel_()
          .id(paymentMethod.id)
          .paymentMethod(paymentMethod)
          .clickListener(clickListener)
      )
    }
  }
}