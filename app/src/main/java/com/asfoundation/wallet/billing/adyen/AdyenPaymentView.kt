package com.asfoundation.wallet.billing.adyen

import android.net.Uri
import android.os.Bundle
import com.adyen.checkout.base.model.payments.request.CardPaymentMethod
import com.adyen.checkout.base.model.payments.response.Action
import com.asfoundation.wallet.ui.iab.PaymentMethod
import io.reactivex.Observable
import java.math.BigDecimal

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
  fun showGenericError()
  fun getMorePaymentMethodsClicks(): Observable<Any>
  fun showMoreMethods()
  fun hideLoadingAndShowView()
  fun finishCardConfiguration(
      paymentMethod: com.adyen.checkout.base.model.paymentmethods.PaymentMethod,
      isStored: Boolean, forget: Boolean, savedInstance: Bundle?)

  fun retrievePaymentData(): Observable<CardPaymentMethod>
  fun showSpecificError(refusalCode: Int)
  fun showProductPrice(fiatAmount: BigDecimal, currencyCode: String)
  fun lockRotation()
  fun setRedirectComponent(action: Action, uid: String)
  fun submitUriResult(uri: Uri)
  fun getPaymentDetails(): Observable<RedirectComponentModel>
  fun forgetCardClick(): Observable<Any>
}
