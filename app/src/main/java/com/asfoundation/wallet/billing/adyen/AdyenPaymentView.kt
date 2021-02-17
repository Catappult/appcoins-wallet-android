package com.asfoundation.wallet.billing.adyen

import android.net.Uri
import android.os.Bundle
import com.adyen.checkout.base.model.payments.response.Action
import com.asfoundation.wallet.billing.address.BillingAddressModel
import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject
import java.math.BigDecimal

interface AdyenPaymentView {

  fun getAnimationDuration(): Long

  fun showLoading(isPreSelected: Boolean, bonus: String)

  fun errorDismisses(): Observable<Any>

  fun buyButtonClicked(): Observable<Any>

  fun showNetworkError()

  fun backEvent(): Observable<Any>

  fun close(bundle: Bundle?)

  fun showSuccess(isPreSelected: Boolean)

  fun showGenericError()

  fun getMorePaymentMethodsClicks(): Observable<Any>

  fun showMoreMethods()

  fun hideLoadingAndShowView(isPreSelected: Boolean, bonus: String)

  fun finishCardConfiguration(
      paymentMethod: com.adyen.checkout.base.model.paymentmethods.PaymentMethod, isStored: Boolean,
      forget: Boolean, savedInstance: Bundle?)

  fun retrievePaymentData(): ReplaySubject<AdyenCardWrapper>

  fun retrieveBillingAddressData(): BillingAddressModel?

  fun billingAddressInput(): Observable<Boolean>

  fun showSpecificError(stringRes: Int)

  fun showCvvError(isPreSelected: Boolean, bonus: String)

  fun showProductPrice(amount: String, currencyCode: String)

  fun lockRotation()

  fun setupRedirectComponent()

  fun submitUriResult(uri: Uri)

  fun getPaymentDetails(): Observable<AdyenComponentResponseModel>

  fun forgetCardClick(): Observable<Any>

  fun hideKeyboard()

  fun adyenErrorCancelClicks(): Observable<Any>

  fun adyenErrorBackClicks(): Observable<Any>

  fun getAdyenSupportLogoClicks(): Observable<Any>

  fun getAdyenSupportIconClicks(): Observable<Any>

  fun showVerification()

  fun handle3DSAction(action: Action)

  fun onAdyen3DSError(): Observable<String>

  fun setup3DSComponent()

  fun showBillingAddress(value: BigDecimal, currency: String, bonus: String, appcAmount: BigDecimal)

  fun setupUi(data: AdyenPaymentData)
}
