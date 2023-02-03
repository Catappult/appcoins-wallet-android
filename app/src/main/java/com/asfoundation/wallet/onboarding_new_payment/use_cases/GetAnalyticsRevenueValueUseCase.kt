package com.asfoundation.wallet.onboarding_new_payment.use_cases

import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import java.math.BigDecimal
import javax.inject.Inject

class GetAnalyticsRevenueValueUseCase @Inject constructor(
  private val inAppPurchaseInteractor: InAppPurchaseInteractor,
) {

  operator fun invoke(transactionBuilder: TransactionBuilder): String {
    return inAppPurchaseInteractor.convertToFiat(
      transactionBuilder.amount().toDouble(),
      BillingAnalytics.EVENT_REVENUE_CURRENCY
    ).blockingGet().amount.setScale(2, BigDecimal.ROUND_UP)
      .toString()
  }
}


