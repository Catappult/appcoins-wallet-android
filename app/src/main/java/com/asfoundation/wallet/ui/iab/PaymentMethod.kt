package com.asfoundation.wallet.ui.iab

import java.math.BigDecimal

open class PaymentMethod(open val id: String, open val label: String,
                         open val iconUrl: String, val fee: PaymentMethodFee?,
                         open val isEnabled: Boolean = true, open var disabledReason: Int? = null) {
  constructor() : this("", "", "", null, false)

  companion object {
    @JvmField
    val APPC: PaymentMethod =
        PaymentMethod("appcoins", "AppCoins (APPC)",
            "https://cdn6.aptoide.com/imgs/a/f/9/af95bd0d14875800231f05dbf1933143_logo.png",
            null)
  }
}

data class PaymentMethodFee(
    val isExact: Boolean,
    val amount: BigDecimal?,
    val currency: String?
)