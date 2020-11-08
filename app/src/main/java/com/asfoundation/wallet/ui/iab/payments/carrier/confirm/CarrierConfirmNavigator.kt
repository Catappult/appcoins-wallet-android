package com.asfoundation.wallet.ui.iab.payments.carrier.confirm

import androidx.fragment.app.FragmentManager
import com.asfoundation.wallet.navigator.UriNavigator

class CarrierConfirmNavigator(private val fragmentManager: FragmentManager,
                              private val uriNavigator: UriNavigator) {

  fun navigateBack() {
    fragmentManager.popBackStack()
  }

  fun navigateToPaymentWebView(paymentUrl: String) {
    uriNavigator.navigateToUri(paymentUrl)
  }
}