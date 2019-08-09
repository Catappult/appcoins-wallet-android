package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import com.adyen.core.models.Amount
import com.adyen.core.models.PaymentMethod
import com.adyen.core.models.paymentdetails.PaymentDetails
import com.asfoundation.wallet.billing.authorization.AdyenAuthorization
import io.reactivex.Observable

interface AdyenAuthorizationView {

  fun getAnimationDuration(): Long
  fun showProduct()
  fun showLoading()
  fun hideLoading()
  fun errorDismisses(): Observable<Any>
  fun paymentMethodDetailsEvent(): Observable<PaymentDetails>
  fun changeCardMethodDetailsEvent(): Observable<PaymentMethod>
  fun showNetworkError()
  fun cancelEvent(): Observable<Any>
  fun showCvcView(amount: Amount, paymentMethod: PaymentMethod)
  fun showCreditCardView(paymentMethod: PaymentMethod, amount: Amount, cvcStatus: Boolean,
                         allowSave: Boolean, publicKey: String, generationTime: String)

  fun close(bundle: Bundle)
  fun showSuccess()
  fun showPaymentRefusedError(adyenAuthorization: AdyenAuthorization)
  fun showGenericError()
  fun getMorePaymentMethodsClicks(): Observable<Any>
  fun showMoreMethods()
  fun onValidFieldStateChange(): Observable<Boolean>
  fun updateButton(valid: Boolean)
}
