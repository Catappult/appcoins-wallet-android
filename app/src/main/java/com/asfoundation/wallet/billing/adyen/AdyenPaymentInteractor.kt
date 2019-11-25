package com.asfoundation.wallet.billing.adyen

import android.os.Bundle
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.billing.adyen.AdyenPaymentService
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single

class AdyenPaymentInteractor(
    private val adyenPaymentService: AdyenPaymentService,
    private val inAppPurchaseInteractor: InAppPurchaseInteractor,
    private val billingMessagesMapper: BillingMessagesMapper) {

  fun loadPaymentInfo(methods: AdyenPaymentService.Methods, value: String,
                      currency: String): Single<PaymentInfoModel> {
    return adyenPaymentService.loadPaymentInfo(methods, value, currency)
  }

  fun makePayment(value: String, currency: String, reference: String, encryptedCardNumber: String?,
                  encryptedExpiryMonth: String?, encryptedExpiryYear: String?,
                  encryptedSecurityCode: String?, type: String,
                  returnUrl: String?): Single<PaymentModel> {
    return adyenPaymentService.makePayment(value, currency, reference, encryptedCardNumber,
        encryptedExpiryMonth, encryptedExpiryYear, encryptedSecurityCode, type,
        returnUrl)
  }

  fun submitRedirect(payload: String?, paymentData: String?): Single<PaymentModel> {
    return adyenPaymentService.submitRedirect(payload, paymentData)
  }

  fun convertToFiat(amount: Double, currency: String): Single<FiatValue> {
    return inAppPurchaseInteractor.convertToFiat(amount, currency)
  }

  fun mapCancellation(): Bundle {
    return billingMessagesMapper.mapCancellation()
  }

  fun removePreSelectedPaymentMethod() {
    inAppPurchaseInteractor.removePreSelectedPaymentMethod()
  }

  fun getCompletePurchaseBundle(type: String, merchantName: String, sku: String?,
                                orderReference: String?, hash: String?,
                                scheduler: Scheduler): Single<Bundle> { //TODO
    return Single.just(Bundle())
  }
}
