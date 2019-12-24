package com.asfoundation.wallet.topup.payment

import android.net.Uri
import android.os.Bundle
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.adyen.checkout.base.model.payments.request.CardPaymentMethod
import com.adyen.checkout.base.model.payments.response.Action
import com.asfoundation.wallet.billing.adyen.RedirectComponentModel
import io.reactivex.Observable

interface AdyenTopUpView {

  fun showValues(value: String, currency: String)

  fun showLoading()

  fun showFinishingLoading()

  fun hideLoading()

  fun showNetworkError()

  fun showGenericError()

  fun updateTopUpButton(valid: Boolean)

  fun showChipsAsDisabled(index: Int)

  fun cancelPayment()

  fun setFinishingPurchase()

  fun errorDismisses(): Observable<Any>

  fun errorCancels(): Observable<Any>

  fun errorPositiveClicks(): Observable<Any>

  fun finishCardConfiguration(paymentMethod: PaymentMethod, isStored: Boolean, forget: Boolean,
                              savedInstanceState: Bundle?)

  fun setRedirectComponent(uid: String, action: Action)

  fun forgetCardClick(): Observable<Any>

  fun submitUriResult(uri: Uri)

  fun getPaymentDetails(): Observable<RedirectComponentModel>

  fun showSpecificError(refusalCode: Int)

  fun topUpButtonClicked(): Observable<Any>

  fun retrievePaymentData(): Observable<CardPaymentMethod>

  fun provideReturnUrl(): String

  fun hideKeyboard()
}
