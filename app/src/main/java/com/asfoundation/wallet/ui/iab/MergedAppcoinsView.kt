package com.asfoundation.wallet.ui.iab

import androidx.annotation.StringRes
import com.asfoundation.wallet.ui.balance.Balance
import io.reactivex.Observable

interface MergedAppcoinsView {

  fun showError(@StringRes errorMessage: Int)

  fun getPaymentSelection(): Observable<String>

  fun hideBonus()

  fun showBonus()

  fun buyClick(): Observable<String>

  fun backClick(): Observable<Any>

  fun backPressed(): Observable<Any>

  fun navigateToAppcPayment()

  fun navigateToCreditsPayment()

  fun navigateToPaymentMethods(preSelectedMethod: PaymentMethodsView.SelectedPaymentMethod)

  fun updateBalanceValues(appcBalance: Balance, creditsBalance: Balance, ethBalance: Balance)
}
