package com.asfoundation.wallet.topup.payment

import android.net.Uri
import android.os.Bundle
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.topup.TopUpActivityView
import com.asfoundation.wallet.ui.iab.Navigator
import io.reactivex.Observable
import java.math.BigDecimal

class PaymentFragmentNavigator(private val uriNavigator: UriNavigator,
                               private val topUpView: TopUpActivityView) : Navigator {

  override fun popView(bundle: Bundle) {
    topUpView.finish(bundle)
  }

  override fun popViewWithError() {
    topUpView.close()
  }

  override fun navigateToUriForResult(redirectUrl: String, transactionUid: String,
                                      domain: String, skuId: String, amount: BigDecimal, type: String) {
    uriNavigator.navigateToUri(redirectUrl, domain, skuId, amount, type)
  }

  override fun uriResults(): Observable<Uri> {
    return uriNavigator.uriResults()
  }
}
