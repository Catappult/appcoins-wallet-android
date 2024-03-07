package com.asfoundation.wallet.billing.adyen

import android.net.Uri
import android.os.Bundle
import com.adyen.checkout.components.model.payments.response.Action
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject
import java.util.Date

interface AdyenPaymentView {

  fun getAnimationDuration(): Long

  fun showProduct()

  fun showLoading()

  fun showLoadingMakingPayment()

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

  fun getMorePaymentMethodsStoredClicks(): Observable<Any>

  fun showMoreMethods()

  fun hideLoadingAndShowView()

  fun finishCardConfiguration(paymentInfoModel: PaymentInfoModel, forget: Boolean)

  fun retrievePaymentData(): ReplaySubject<AdyenCardWrapper>

  fun showSpecificError(stringRes: Int, backToCard: Boolean = false)

  fun showNoNetworkError(backToCard: Boolean = false)

  fun showVerificationError(isWalletVerified: Boolean)

  fun showCvvError()

  fun showBackToCard()

  fun showProductPrice(amount: String, currencyCode: String)

  fun lockRotation()

  fun setupRedirectComponent()

  fun submitUriResult(uri: Uri)

  fun getPaymentDetails(): Observable<AdyenComponentResponseModel>

  fun forgetCardClick(): Observable<Any>

  fun forgetStoredCardClick(): Observable<Any>

  fun hideKeyboard()

  fun adyenErrorCancelClicks(): Observable<Any>

  fun adyenErrorBackClicks(): Observable<Any>

  fun adyenErrorBackToCardClicks(): Observable<Any>

  fun getAdyenSupportLogoClicks(): Observable<Any>

  fun getAdyenSupportIconClicks(): Observable<Any>

  fun getVerificationClicks(): Observable<Boolean>

  fun showVerification(isWalletVerified: Boolean)

  fun handle3DSAction(action: Action)

  fun handleCreditCardNeedCVC(needCVC: Boolean)

  fun onAdyen3DSError(): Observable<String>

  fun setup3DSComponent()

  fun shouldStoreCard(): Boolean
  fun restartFragment()

  fun showCvcRequired()

}
