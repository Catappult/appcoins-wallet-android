package com.asfoundation.wallet.topup

import android.net.Uri
import android.os.Bundle
import com.asfoundation.wallet.billing.adyen.PaymentType

interface TopUpActivityView {
  fun showTopUpScreen()

  fun navigateToPayment(paymentType: PaymentType, data: TopUpData,
                        selectedCurrency: String, origin: String, transactionType: String)

  fun finish(data: Bundle)

  fun close()

  fun acceptResult(uri: Uri)

  fun showToolbar()
}
