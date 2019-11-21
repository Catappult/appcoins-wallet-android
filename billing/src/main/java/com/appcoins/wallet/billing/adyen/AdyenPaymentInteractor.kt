package com.appcoins.wallet.billing.adyen

import io.reactivex.Single

class AdyenPaymentInteractor(private val adyenPaymentService: AdyenPaymentService) {

  fun loadPaymentInfo(methods: AdyenPaymentService.Methods, value: String,
                      currency: String): Single<PaymentInfoModel> {
    return adyenPaymentService.loadPaymentInfo(methods, value, currency)
  }

  fun makePayment(value: String, currency: String, reference: String, encryptedCardNumber: String?,
                  encryptedExpiryMonth: String?, encryptedExpiryYear: String?,
                  encryptedSecurityCode: String?, holderName: String?, type: String,
                  returnUrl: String?): Single<PaymentModel> {
    return adyenPaymentService.makePayment(value, currency, reference, encryptedCardNumber,
        encryptedExpiryMonth, encryptedExpiryYear, encryptedSecurityCode, holderName, type,
        returnUrl)
  }
}
