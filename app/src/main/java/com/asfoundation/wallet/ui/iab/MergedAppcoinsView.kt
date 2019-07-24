package com.asfoundation.wallet.ui.iab

import io.reactivex.Observable

interface MergedAppcoinsView {

  fun showError(errorMessage: Int)

  fun getPaymentSelection(): Observable<String>

  fun hideBonus()

  fun showBonus()

  fun buyClick(): Observable<String>

  fun backClick(): Observable<Any>

  fun navigateToAppcPayment()

  fun navigateToCreditsPayment()
}
