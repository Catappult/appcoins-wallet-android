package com.asfoundation.wallet.onboarding_new_payment

import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import java.math.BigDecimal

fun PaymentType.mapToAnalytics(): String =
  if (this.name == PaymentType.CARD.name) {
    BillingAnalytics.PAYMENT_METHOD_CC
  } else {
    BillingAnalytics.PAYMENT_METHOD_PAYPAL
  }

fun PaymentType.mapToService(): AdyenPaymentRepository.Methods =
  when (this.name) {
    PaymentType.CARD.name -> {
      AdyenPaymentRepository.Methods.CREDIT_CARD
    }
    PaymentType.GIROPAY.name -> {
      AdyenPaymentRepository.Methods.GIROPAY
    }
    else -> {
      AdyenPaymentRepository.Methods.PAYPAL
    }
  }

fun ForecastBonusAndLevel.getPurchaseBonusMessage(formatter: CurrencyFormatUtils): String {
  var scaledBonus = this.amount.stripTrailingZeros()
    .setScale(CurrencyFormatUtils.FIAT_SCALE, BigDecimal.ROUND_DOWN)
  val newCurrencyString =
    if (scaledBonus < BigDecimal("0.01")) "~${this.currency}" else this.currency
  scaledBonus = scaledBonus.max(BigDecimal("0.01"))
  val formattedBonus = formatter.formatCurrency(scaledBonus, WalletCurrency.FIAT)
  return newCurrencyString + formattedBonus
}
