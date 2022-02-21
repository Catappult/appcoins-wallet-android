package com.asfoundation.wallet.topup.adyen

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.topup.TopUpActivityView
import com.asfoundation.wallet.ui.iab.Navigator
import io.reactivex.Observable
import javax.inject.Inject

class TopUpNavigator @Inject constructor(private val fragmentManager: FragmentManager,
                     private val fragment: Fragment) : Navigator {

  override fun popView(bundle: Bundle) {
    (fragment.activity as TopUpActivityView).finish(bundle)
  }

  override fun popViewWithError() {
    (fragment.activity as TopUpActivityView).close(false)
  }

  override fun navigateToUriForResult(redirectUrl: String) {
    (fragment.activity as UriNavigator).navigateToUri(redirectUrl)
  }

  override fun uriResults(): Observable<Uri> {
    return (fragment.activity as UriNavigator).uriResults()
  }

  override fun navigateBack() {
    if (fragmentManager.backStackEntryCount != 0) {
      fragmentManager.popBackStack()
    } else {
      (fragment.activity as TopUpActivityView).close()
    }
  }
}
