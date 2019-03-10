package com.asfoundation.wallet.topup.payment

import android.os.Bundle
import com.adyen.core.models.Amount
import com.adyen.core.models.PaymentMethod
import com.adyen.core.models.paymentdetails.PaymentDetails
import com.asfoundation.wallet.billing.authorization.AdyenAuthorization
import io.reactivex.Observable

interface PaymentAuthView {

  fun showProduct()

  fun showLoading()

  fun hideLoading()

  fun errorDismisses(): Observable<Any>

  fun paymentMethodDetailsEvent(): Observable<PaymentDetails>

  fun changeCardMethodDetailsEvent(): Observable<PaymentMethod>

  fun showNetworkError()

  fun showCvcView(amount: Amount, paymentMethod: PaymentMethod)

  fun showCreditCardView(paymentMethod: PaymentMethod, amount: Amount, cvcStatus: Boolean,
                         allowSave: Boolean, publicKey: String, generationTime: String)

  fun close()

  fun showSuccess()

  fun showPaymentRefusedError(adyenAuthorization: AdyenAuthorization)

  fun showGenericError()
}
