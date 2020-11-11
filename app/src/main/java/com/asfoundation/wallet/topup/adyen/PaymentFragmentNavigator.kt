package com.asfoundation.wallet.topup.adyen

import android.net.Uri
import android.os.Bundle
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.topup.TopUpActivityView
import com.asfoundation.wallet.ui.iab.Navigator
import io.reactivex.Observable

class PaymentFragmentNavigator(private val uriNavigator: UriNavigator,
                               private val topUpView: TopUpActivityView) : Navigator {

  override fun popView(bundle: Bundle) {
    topUpView.finish(bundle)
  }

  override fun popViewWithError() {
    topUpView.close(false)
  }

  override fun navigateToUriForResult(redirectUrl: String) {
    uriNavigator.navigateToUri(redirectUrl)
  }

  override fun uriResults(): Observable<Uri> {
    return uriNavigator.uriResults()
  }
}
