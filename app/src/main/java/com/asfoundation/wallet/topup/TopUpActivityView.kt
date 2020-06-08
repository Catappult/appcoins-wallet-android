package com.asfoundation.wallet.topup

import android.net.Uri
import android.os.Bundle
import com.asfoundation.wallet.billing.adyen.PaymentType

interface TopUpActivityView {
  fun showTopUpScreen()

  fun navigateToAdyenPayment(paymentType: PaymentType, data: TopUpPaymentData)

  fun navigateToLocalPayment(paymentId: String, icon: String, label: String,
                             topUpData: TopUpPaymentData)

  fun finish(data: Bundle)

  fun navigateBack()

  fun close(navigateToTransactions: Boolean)

  fun acceptResult(uri: Uri)

  fun showToolbar()

  fun lockOrientation()

  fun unlockRotation()

  fun cancelPayment()

  fun setFinishingPurchase()
}
