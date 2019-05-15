package com.asfoundation.wallet.topup.payment

import com.adyen.core.models.PaymentMethod
import com.adyen.core.models.paymentdetails.PaymentDetails
import com.asfoundation.wallet.billing.authorization.AdyenAuthorization
import io.reactivex.Observable

interface PaymentAuthView {

  fun showValues()

  fun showLoading()

  fun hideLoading()

  fun errorDismisses(): Observable<Any>

  fun paymentMethodDetailsEvent(): Observable<PaymentDetails>

  fun changeCardMethodDetailsEvent(): Observable<PaymentMethod>

  fun showNetworkError()

  fun showCvcView(paymentMethod: PaymentMethod)

  fun showCreditCardView(paymentMethod: PaymentMethod, cvcStatus: Boolean, allowSave: Boolean,
                         publicKey: String, generationTime: String)

  fun close()

  fun showPaymentRefusedError(adyenAuthorization: AdyenAuthorization)

  fun showGenericError()

  fun onValidFieldStateChange(): Observable<Boolean>?

  fun updateTopUpButton(valid: Boolean)
}
