package com.asfoundation.wallet.ui.iab

import io.reactivex.Observable

interface EarnAppcoinsView {

  fun backButtonClick(): Observable<Any>
  fun discoverButtonClick(): Observable<Any>
  fun navigateBack(preSelectedMethod: PaymentMethodsView.SelectedPaymentMethod)
  fun navigateToAptoide()
  fun backPressed(): Observable<Any>
}
