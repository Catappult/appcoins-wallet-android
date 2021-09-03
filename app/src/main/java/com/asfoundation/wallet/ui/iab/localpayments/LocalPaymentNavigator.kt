package com.asfoundation.wallet.ui.iab.localpayments

import android.net.Uri
import com.asfoundation.wallet.navigator.UriNavigator
import io.reactivex.Observable

class LocalPaymentNavigator(private val uriNavigator: UriNavigator) {

  fun navigateToUriForResult(url: String?) = uriNavigator.navigateToUri(url)

  fun uriResults(): Observable<Uri> = uriNavigator.uriResults()
}
