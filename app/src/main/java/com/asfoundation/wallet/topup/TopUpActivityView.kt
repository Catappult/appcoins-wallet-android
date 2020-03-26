package com.asfoundation.wallet.topup

import android.net.Uri
import android.os.Bundle
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.ui.iab.FiatValue

interface TopUpActivityView {
  fun showTopUpScreen()

  fun navigateToPayment(paymentType: PaymentType, data: TopUpData, selectedCurrency: String,
                        transactionType: String, bonusValue: String, defaultValues: List<FiatValue>,
                        gamificationLevel: Int)

  fun finish(data: Bundle)

  fun close(navigateToTransactions: Boolean)

  fun acceptResult(uri: Uri)

  fun showToolbar()

  fun lockOrientation()

  fun unlockRotation()

  fun cancelPayment()

  fun setFinishingPurchase()
}
