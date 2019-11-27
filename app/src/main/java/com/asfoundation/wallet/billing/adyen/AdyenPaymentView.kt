package com.asfoundation.wallet.billing.adyen

import android.net.Uri
import android.os.Bundle
import com.adyen.checkout.base.model.payments.response.Action
import com.asfoundation.wallet.billing.authorization.AdyenAuthorization
import com.asfoundation.wallet.ui.iab.PaymentMethod
import io.reactivex.Observable
import org.json.JSONObject

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
      paymentMethod: com.adyen.checkout.base.model.paymentmethods.PaymentMethod,
      isStored: Boolean,
      forgeted: Boolean)

  fun retrievePaymentData(): Observable<PaymentData>
  fun showSpecificError(refusalCode: Int)
  fun showProductPrice(amount: String, currencyCode: String)
  fun lockRotation()
  fun setRedirectComponent(action: Action, paymentDetailsData: String?)
  fun submitUriResult(uri: Uri)
  fun getPaymentDetails(): Observable<JSONObject>
  fun getPaymentDetailsData(): Observable<String?>
  fun forgetCardClick(): Observable<Any>
}
