package com.asfoundation.wallet.ui.iab

import android.net.Uri
import io.reactivex.Observable

interface EarnAppcoinsView {

  fun backButtonClick(): Observable<Any>
  fun discoverButtonClick(): Observable<Any>
  fun navigateBack(preSelectedMethod: PaymentMethodsView.SelectedPaymentMethod)
  fun navigateToDeepLink(uri: Uri)
  fun backPressed(): Observable<Any>
}
