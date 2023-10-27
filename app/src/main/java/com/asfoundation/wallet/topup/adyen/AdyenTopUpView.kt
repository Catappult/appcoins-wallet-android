package com.asfoundation.wallet.topup.adyen

import android.net.Uri
import com.adyen.checkout.components.model.payments.response.Action
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.asfoundation.wallet.billing.address.BillingAddressModel
import com.asfoundation.wallet.billing.adyen.AdyenCardWrapper
import com.asfoundation.wallet.billing.adyen.AdyenComponentResponseModel
import io.reactivex.Observable
import java.math.BigDecimal

interface AdyenTopUpView {

  fun showValues(value: String, currency: String)

  fun showLoading()

  fun hideLoading()

  fun showNetworkError()

  fun showInvalidCardError()

  fun showSecurityValidationError()

  fun showOutdatedCardError()

  fun showAlreadyProcessedError()

  fun showPaymentError()

  fun updateTopUpButton(valid: Boolean)

  fun cancelPayment()

  fun setFinishingPurchase(newState: Boolean)

  fun finishCardConfiguration(paymentInfoModel: PaymentInfoModel, forget: Boolean)

  fun setupRedirectComponent()

  fun forgetCardClick(): Observable<Any>

  fun submitUriResult(uri: Uri)

  fun getPaymentDetails(): Observable<AdyenComponentResponseModel>

  fun showSpecificError(stringRes: Int)

  fun showVerificationError()

  fun showCvvError()

  fun topUpButtonClicked(): Observable<Any>

  fun billingAddressInput(): Observable<Boolean>

  fun retrievePaymentData(): Observable<AdyenCardWrapper>

  fun retrieveBillingAddressData(): BillingAddressModel?

  fun hideKeyboard()

  fun getTryAgainClicks(): Observable<Any>

  fun getSupportClicks(): Observable<Any>

  fun getVerificationClicks(): Observable<Any>

  fun lockRotation()

  fun retryClick(): Observable<Any>

  fun hideErrorViews()

  fun showRetryAnimation()

  fun setupUi()

  fun showBonus(bonus: BigDecimal, currency: String)

  fun showVerification()

  fun handle3DSAction(action: Action)

  fun onAdyen3DSError(): Observable<String>

  fun setup3DSComponent()

  fun navigateToBillingAddress(fiatAmount: String, fiatCurrency: String)

  fun shouldStoreCard(): Boolean
}
