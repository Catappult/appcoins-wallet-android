package com.asfoundation.wallet.billing.adyen

import android.net.Uri
import android.os.Bundle
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.asfoundation.wallet.billing.address.BillingAddressModel
import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject
import java.math.BigDecimal
import java.util.*

interface AdyenPaymentView {

  fun getAnimationDuration(): Long

  fun showProduct()

  fun showLoading()

  fun errorDismisses(): Observable<Any>

  fun buyButtonClicked(): Observable<Any>

  fun showNetworkError()

  fun backEvent(): Observable<Any>

  fun close(bundle: Bundle?)

  fun showSuccess(renewal: Date?)

  fun showGenericError()

  fun showInvalidCardError()

  fun showSecurityValidationError()

  fun showOutdatedCardError()

  fun showAlreadyProcessedError()

  fun showPaymentError()

  fun getMorePaymentMethodsClicks(): Observable<Any>

  fun showMoreMethods()

  fun hideLoadingAndShowView()

  fun finishCardConfiguration(paymentInfoModel: PaymentInfoModel, forget: Boolean)

  fun retrievePaymentData(): ReplaySubject<AdyenCardWrapper>

  fun retrieveBillingAddressData(): BillingAddressModel?

  fun billingAddressInput(): Observable<Boolean>

  fun showSpecificError(stringRes: Int)

  fun showVerificationError(isWalletVerified: Boolean)

  fun showCvvError()

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

  fun getVerificationClicks(): Observable<Boolean>

  fun showVerification(isWalletVerified: Boolean)

  fun handle3DSAction(action: Action)

  fun onAdyen3DSError(): Observable<String>

  fun setup3DSComponent()

  fun setupGooglePayComponent(paymentMethod: PaymentMethod)

  fun startGooglePay()

  fun showBillingAddress(value: BigDecimal, currency: String)
}
