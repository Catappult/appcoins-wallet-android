package com.asfoundation.wallet.topup

import android.net.Uri
import android.os.Bundle
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.ui.iab.FiatValue

interface TopUpActivityView {
  fun showTopUpScreen()

  fun navigateToPayment(paymentType: PaymentType,
                        data: TopUpData,
                        selectedCurrency: String, origin: String,
                        transactionType: String, bonusValue: String,
                        selectedChip: Int, chipValues: List<FiatValue>,
                        chipAvailability: Boolean)

  fun finish(data: Bundle)

  fun close(navigateToTransactions: Boolean)

  fun acceptResult(uri: Uri)

  fun showToolbar()

  fun lockOrientation()

  fun unlockRotation()

  fun cancelPayment()

  fun setFinishingPurchase()
}
