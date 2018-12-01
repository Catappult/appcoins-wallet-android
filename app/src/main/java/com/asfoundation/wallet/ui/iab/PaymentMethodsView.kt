package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import io.reactivex.Observable
import java.io.IOException

interface PaymentMethodsView {
    fun showPaymentMethods(paymentMethods: MutableList<PaymentMethod>, fiatValue: FiatValue, isDonation: Boolean)
    fun showError()
    @Throws(IOException::class)
    fun finish(purchase: Purchase)

    fun showLoading()
    fun hideLoading()
    fun getCancelClick(): Observable<Any>
    fun close(bundle: Bundle)
    fun errorDismisses(): Observable<Any>
    fun setupUiCompleted(): Observable<Boolean>
    fun showProcessingLoadingDialog()
}
