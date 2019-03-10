package com.asfoundation.wallet.topup

import android.os.Bundle
import com.asfoundation.wallet.billing.adyen.PaymentType

interface TopUpActivityView {
  fun showTopUpScreen()

  fun navigateToAdyen(isBds: Boolean, currency: String, paymentType: PaymentType)

  fun finish(data: Bundle)

  fun showError()

  fun close()

  fun navigateToAdyenAuthorization(isBds: Boolean, currency: String,
                                            paymentType: PaymentType)

  fun navigateToWebViewAuthorization(url: String)
}
