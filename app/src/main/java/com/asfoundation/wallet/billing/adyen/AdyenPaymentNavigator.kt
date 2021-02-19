package com.asfoundation.wallet.billing.adyen

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.Navigator
import com.asfoundation.wallet.ui.iab.vouchers.VouchersSuccessFragment
import io.reactivex.Observable

class AdyenPaymentNavigator(private val fragmentManager: FragmentManager,
                            private val navigator: Navigator) {

  fun navigateToVoucherSuccess(bonus: String, code: String, redeemLink: String) {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            VouchersSuccessFragment.newInstance(code, redeemLink, bonus))
        .commit()
  }

  fun uriResults(): Observable<Uri> = navigator.uriResults()

  fun navigateToUriForResult(redirectUrl: String?) = navigator.navigateToUriForResult(redirectUrl)

  fun popViewWithError() = navigator.popViewWithError()

  fun popView(bundle: Bundle) = navigator.popView(bundle)
}
