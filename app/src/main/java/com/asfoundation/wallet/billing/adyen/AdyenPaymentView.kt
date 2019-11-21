package com.asfoundation.wallet.billing.adyen

import android.os.Bundle
import com.adyen.checkout.base.model.payments.Amount
import com.asfoundation.wallet.billing.authorization.AdyenAuthorization
import com.asfoundation.wallet.ui.iab.PaymentMethod
import io.reactivex.Observable
import org.jetbrains.annotations.NotNull

interface AdyenPaymentView {

  fun getAnimationDuration(): Long
  fun showProduct()
  fun showLoading()
  fun errorDismisses(): Observable<Any>
  fun buyButtonClicked(): Observable<Any>
  fun changeCardMethodDetailsEvent(): Observable<PaymentMethod>
  fun showNetworkError()
  fun backEvent(): Observable<Any>
  fun showCvcView(amount: Amount, paymentMethod: PaymentMethod)
  fun showCreditCardView(paymentMethod: PaymentMethod, amount: Amount, cvcStatus: Boolean,
                         allowSave: Boolean, publicKey: String, generationTime: String)

  fun close(bundle: Bundle?)
  fun showSuccess()
  fun showPaymentRefusedError(adyenAuthorization: @NotNull AdyenAuthorization?)
  fun showGenericError()
  fun getMorePaymentMethodsClicks(): @NotNull Observable<Any?>?
  fun showMoreMethods()
  fun onValidFieldStateChange(): Observable<Boolean?>?
  fun updateButton(valid: Boolean)
  fun hideLoading()
  fun finishCardConfiguration(
      paymentMethod: com.adyen.checkout.base.model.paymentmethods.PaymentMethod)

  fun retrievePaymentData(): Observable<PaymentData>
}
