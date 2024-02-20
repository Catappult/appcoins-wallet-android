package com.asfoundation.wallet.ui.iab.payments.carrier.status

import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.ui.iab.IabActivity
import com.asfoundation.wallet.ui.iab.payments.carrier.verify.CarrierVerifyFragment
import com.asfoundation.wallet.ui.iab.payments.common.error.IabErrorFragment
import io.reactivex.Observable
import javax.inject.Inject

class CarrierPaymentNavigator
@Inject
constructor(private val fragmentManager: FragmentManager, fragment: Fragment) {

  private val uriNavigator = fragment.activity as UriNavigator
  private val iabActivity = fragment.activity as IabActivity

  fun navigateToPaymentWebView(paymentUrl: String) = uriNavigator.navigateToUri(paymentUrl)

  fun uriResults(): Observable<Uri> = uriNavigator.uriResults()

  fun navigateToVerification() {
    iabActivity.showVerification(false)
  }

  fun navigateToError(@StringRes messageStringRes: Int) {
    fragmentManager
        .beginTransaction()
        .replace(
            R.id.fragment_container,
            IabErrorFragment.newInstance(messageStringRes, CarrierVerifyFragment.BACKSTACK_NAME))
        .addToBackStack(null)
        .commit()
  }

  fun finishPayment(bundle: Bundle) = iabActivity.finish(bundle)
}
