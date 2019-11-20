package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import io.reactivex.Single

class AdyenPaymentInteractor(private val adyenPaymentService: AdyenPaymentService) {

  fun loadPaymentInfo(methods: AdyenPaymentService.Methods, value: String,
                      currency: String): Single<PaymentInfoModel> {
    return adyenPaymentService.loadPaymentInfo(methods, value, currency)
  }

  fun makePayment(value: String, reference: String, paymentMethod: PaymentMethod, returnUrl: String): Single<PaymentModel>{
    return adyenPaymentService.makePayment(value, reference, paymentMethod, returnUrl)
  }
}
