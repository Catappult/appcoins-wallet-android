package com.asfoundation.wallet.onboarding_new_payment.payment_methods.list

import com.airbnb.epoxy.Typed3EpoxyController
import com.asfoundation.wallet.onboarding_new_payment.payment_methods.list.model.PaymentMethodModel_
import com.asfoundation.wallet.ui.iab.PaymentMethod
import com.asfoundation.wallet.ui.iab.PaymentMethodsMapper

class PaymentMethodsController :
  Typed3EpoxyController<List<PaymentMethod>, List<PaymentMethod>, PaymentMethodsMapper>() {

  var clickListener: ((PaymentMethodClick) -> Unit)? = null

  override fun buildModels(
    model: List<PaymentMethod>,
    other: List<PaymentMethod>,
    mapper: PaymentMethodsMapper
  ) {
    for (paymentMethod in model) {
      /**
       * if condition to only show both credit card and paypal without making bigger changes
       */
      if (paymentMethod.id != "ask_friend" && paymentMethod.id != "onebip") {
        add(
          PaymentMethodModel_()
            .id(paymentMethod.id)
            .paymentMethod(paymentMethod)
            .paymentMethodMapper(mapper)
            .clickListener(clickListener)
        )
      }
    }
    if (other.isNotEmpty()) {
      add(IncompletePaymentMethodsGroup(other, clickListener))
    }
  }
}