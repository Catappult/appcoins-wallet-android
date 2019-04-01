package com.asfoundation.wallet.topup

import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.topup.TopUpData.Companion.DEFAULT_VALUE
import java.io.Serializable

data class TopUpData(var currency: CurrencyData,
                     var selectedCurrency: String, var paymentMethod: PaymentType? = null) :
    Serializable {
  companion object {
    const val FIAT_CURRENCY = "FIAT_CURRENCY"
    const val APPC_C_CURRENCY = "APPC_C_CURRENCY"
    const val DEFAULT_VALUE = "--"
  }
}

data class CurrencyData(val fiatCurrencyCode: String = DEFAULT_VALUE,
                        val fiatCurrencySymbol: String = DEFAULT_VALUE,
                        var fiatValue: String = DEFAULT_VALUE, val appcCode: String = DEFAULT_VALUE,
                        val appcSymbol: String = DEFAULT_VALUE,
                        var appcValue: String = DEFAULT_VALUE) : Serializable
