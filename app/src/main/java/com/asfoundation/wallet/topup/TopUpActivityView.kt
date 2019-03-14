package com.asfoundation.wallet.topup

import android.os.Bundle
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.entity.TransactionBuilder

interface TopUpActivityView {
  fun showTopUpScreen()

  fun navigateToPayment(paymentType: PaymentType, data: TopUpData,
                        selectedCurrency: String, transaction: TransactionBuilder)

  fun finish(data: Bundle)

  fun close()
}
