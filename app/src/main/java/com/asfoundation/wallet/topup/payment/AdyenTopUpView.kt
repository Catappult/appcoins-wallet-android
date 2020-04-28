package com.asfoundation.wallet.topup.payment

import android.net.Uri
import android.os.Bundle
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.adyen.checkout.base.model.payments.response.Action
import com.asfoundation.wallet.billing.adyen.AdyenCardWrapper
import com.asfoundation.wallet.billing.adyen.RedirectComponentModel
import io.reactivex.Observable

interface AdyenTopUpView {

  fun showValues(value: String, currency: String)

  fun showLoading()

  fun showFinishingLoading()

  fun hideLoading()

  fun showNetworkError()

  fun updateTopUpButton(valid: Boolean)

  fun cancelPayment()

  fun setFinishingPurchase()

  fun finishCardConfiguration(paymentMethod: PaymentMethod, isStored: Boolean, forget: Boolean,
                              savedInstanceState: Bundle?)

  fun setRedirectComponent(uid: String, action: Action)

  fun forgetCardClick(): Observable<Any>

  fun submitUriResult(uri: Uri)

  fun getPaymentDetails(): Observable<RedirectComponentModel>

  fun showSpecificError(stringRes: Int)

  fun showCvvError()

  fun topUpButtonClicked(): Observable<Any>

  fun retrievePaymentData(): Observable<AdyenCardWrapper>

  fun hideKeyboard()

  fun getTryAgainClicks(): Observable<Any>

  fun getSupportClicks(): Observable<Any>

  fun hideSpecificError()

  fun retryClick(): Observable<Any>

  fun hideNoNetworkError()

  fun showRetryAnimation()
}
