package com.asfoundation.wallet.ui.iab.payments.carrier.confirm

import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentManager
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.ui.iab.IabActivity
import io.reactivex.Observable

class CarrierConfirmNavigator(private val fragmentManager: FragmentManager,
                              private val uriNavigator: UriNavigator,
                              private val iabActivity: IabActivity) {

  fun navigateBack() {
    fragmentManager.popBackStack()
  }

  fun navigateToPaymentWebView(paymentUrl: String) {
    uriNavigator.navigateToUri(paymentUrl)
  }

  fun uriResults(): Observable<Uri> {
    return uriNavigator.uriResults()
  }


  fun navigateToWalletValidation(@StringRes messageStringRes: Int) {
    iabActivity.showWalletValidation(messageStringRes)
  }

  fun navigateToError(@StringRes messageStringRes: Int) {
    // TODO
  }

  fun finishPayment(bundle: Bundle) {
    iabActivity.finish(bundle)
  }
}