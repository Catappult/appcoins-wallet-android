package com.asfoundation.wallet.ui.iab.localpayments

import android.net.Uri
import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.ui.iab.vouchers.VouchersSuccessFragment
import io.reactivex.Observable

class LocalPaymentNavigator(private val fragmentManager: FragmentManager,
                            private val uriNavigator: UriNavigator) {

  fun navigateToUriForResult(url: String?) = uriNavigator.navigateToUri(url)

  fun navigateToVouchersSuccess(code: String, link: String, bonus: String) {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container, VouchersSuccessFragment.newInstance(code, link, bonus))
        .commit()
  }

  fun uriResults(): Observable<Uri> = uriNavigator.uriResults()
}
