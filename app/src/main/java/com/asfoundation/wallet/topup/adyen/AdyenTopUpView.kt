package com.asfoundation.wallet.topup.adyen

import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.adyen.checkout.base.model.payments.response.Action
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

  fun updateTopUpButton(valid: Boolean)

  fun cancelPayment()

  fun setFinishingPurchase()

  fun finishCardConfiguration(paymentMethod: PaymentMethod, isStored: Boolean, forget: Boolean,
                              savedInstanceState: Bundle?)

  fun setupRedirectComponent()

  fun forgetCardClick(): Observable<Any>

  fun submitUriResult(uri: Uri)

  fun getPaymentDetails(): Observable<AdyenComponentResponseModel>

  fun showSpecificError(stringRes: Int)

  fun showCvvError()

  fun topUpButtonClicked(): Observable<Any>

  fun billingAddressInput(): Observable<Boolean>

  fun retrievePaymentData(): Observable<AdyenCardWrapper>

  fun retrieveBillingAddressData(): BillingAddressModel?

  fun hideKeyboard()

  fun getTryAgainClicks(): Observable<Any>

  fun getSupportClicks(): Observable<Any>

  fun lockRotation()

  fun retryClick(): Observable<Any>

  fun hideErrorViews()

  fun showRetryAnimation()

  fun setupUi()

  fun showBonus(bonus: BigDecimal, currency: String)

  fun showWalletValidation(@StringRes error: Int)

  fun handle3DSAction(action: Action)

  fun onAdyen3DSError(): Observable<String>

  fun setup3DSComponent()

  fun navigateToBillingAddress(fiatAmount: String, fiatCurrency: String)
}
