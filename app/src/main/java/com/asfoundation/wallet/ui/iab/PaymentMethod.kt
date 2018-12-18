package com.asfoundation.wallet.ui.iab

data class PaymentMethod(val id: String, val label: String, val iconUrl: String,
                         val isEnable: Boolean) {
    companion object {
        @JvmField
        val APPC: PaymentMethod = PaymentMethod("appcoins", "AppCoins (APPC)", "https://cdn6.aptoide.com/imgs/a/f/9/af95bd0d14875800231f05dbf1933143_logo.png", true)
    }
}
