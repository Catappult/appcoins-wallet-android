package com.asfoundation.wallet.onboarding_new_payment.payment_methods.list

import com.airbnb.epoxy.Typed2EpoxyController
import com.asfoundation.wallet.onboarding_new_payment.payment_methods.list.model.PaymentMethodModel_
import com.asfoundation.wallet.ui.iab.PaymentMethod
import com.asfoundation.wallet.ui.iab.PaymentMethodsMapper

class PaymentMethodsController :
  Typed2EpoxyController<List<PaymentMethod>, PaymentMethodsMapper>() {

  var clickListener: ((PaymentMethodClick) -> Unit)? = null

  override fun buildModels(model: List<PaymentMethod>, mapper: PaymentMethodsMapper) {
    for (paymentMethod in model) {
      /**
       * if to only show both credit card and paypal without making bigger changes
       */
      if (paymentMethod.id == "credit_card" || paymentMethod.id == "paypal") {
        add(
          PaymentMethodModel_()
            .id(paymentMethod.id)
            .paymentMethod(paymentMethod)
            .paymentMethodMapper(mapper)
            .clickListener(clickListener)
        )
      }
    }
  }
}