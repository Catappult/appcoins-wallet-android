package com.asfoundation.wallet.billing.adyen

import android.os.Bundle
import com.asfoundation.wallet.billing.authorization.AdyenAuthorization
import com.asfoundation.wallet.ui.iab.PaymentMethod
import io.reactivex.Observable

interface AdyenPaymentView {

  fun getAnimationDuration(): Long
  fun showProduct()
  fun showLoading()
  fun errorDismisses(): Observable<Any>
  fun buyButtonClicked(): Observable<Any>
  fun changeCardMethodDetailsEvent(): Observable<PaymentMethod>
  fun showNetworkError()
  fun backEvent(): Observable<Any>
  fun close(bundle: Bundle?)
  fun showSuccess()
  fun showPaymentRefusedError(adyenAuthorization: AdyenAuthorization)
  fun showGenericError()
  fun getMorePaymentMethodsClicks(): Observable<Any>
  fun showMoreMethods()
  fun onValidFieldStateChange(): Observable<Boolean?>?
  fun hideLoading()
  fun finishCardConfiguration(
      paymentMethod: com.adyen.checkout.base.model.paymentmethods.PaymentMethod)

  fun retrievePaymentData(): Observable<PaymentData>
  fun showSpecificError(refusalCode: Int)
  fun showProductPrice(amount: String, currencyCode: String)
}
