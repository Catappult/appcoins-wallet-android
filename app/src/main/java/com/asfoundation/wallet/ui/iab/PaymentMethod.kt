package com.asfoundation.wallet.ui.iab

import com.asfoundation.wallet.ui.iab.PaymentMethodsView.SelectedPaymentMethod

data class PaymentMethod(val id: SelectedPaymentMethod, val label: String,
                         val iconUrl: String, val isEnabled: Boolean = true) {

  companion object {
    @JvmField
    val APPC: PaymentMethod =
        PaymentMethod(SelectedPaymentMethod.APPC, "AppCoins (APPC)",
            "https://cdn6.aptoide.com/imgs/a/f/9/af95bd0d14875800231f05dbf1933143_logo.png")
  }
}

