package com.asfoundation.wallet.ui.iab.localpayments

import android.net.Uri
import androidx.fragment.app.Fragment
import com.asfoundation.wallet.navigator.UriNavigator
import io.reactivex.Observable
import javax.inject.Inject

class LocalPaymentNavigator @Inject constructor(private val fragment: Fragment) {

  fun navigateToUriForResult(url: String?) = (fragment.activity as UriNavigator).navigateToUri(url)

  fun uriResults(): Observable<Uri> = (fragment.activity as UriNavigator).uriResults()
}
