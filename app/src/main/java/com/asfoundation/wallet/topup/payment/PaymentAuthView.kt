package com.asfoundation.wallet.topup.payment

import com.adyen.core.models.PaymentMethod
import com.adyen.core.models.paymentdetails.PaymentDetails
import com.asfoundation.wallet.billing.authorization.AdyenAuthorization
import io.reactivex.Observable

interface PaymentAuthView {

  fun showValues(value: String, currency: String)

  fun showLoading()

  fun showFinishingLoading()

  fun hideLoading()

  fun errorDismisses(): Observable<Any>

  fun errorCancels(): Observable<Any>

  fun paymentMethodDetailsEvent(): Observable<PaymentDetails>

  fun changeCardMethodDetailsEvent(): Observable<PaymentMethod>

  fun showNetworkError()

  fun showCvcView(paymentMethod: PaymentMethod, value: String, currency: String)

  fun showCreditCardView(paymentMethod: PaymentMethod, value: String, currency: String,
                         cvcStatus: Boolean, allowSave: Boolean, publicKey: String,
                         generationTime: String)

  fun showPaymentRefusedError(adyenAuthorization: AdyenAuthorization)

  fun showGenericError()

  fun onValidFieldStateChange(): Observable<Boolean>?

  fun updateTopUpButton(valid: Boolean)
}
