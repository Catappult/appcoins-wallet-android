package com.asfoundation.wallet.topup

import com.asfoundation.wallet.topup.TopUpData.Companion.DEFAULT_VALUE
import java.io.Serializable
import java.math.BigDecimal

data class TopUpData(var currency: CurrencyData,
                     var selectedCurrencyType: String,
                     var paymentMethod: PaymentTypeInfo? = null,
                     var bonusValue: BigDecimal = BigDecimal.ZERO) :
    Serializable {
  companion object {
    const val FIAT_CURRENCY = "FIAT_CURRENCY"
    const val APPC_C_CURRENCY = "APPC_C_CURRENCY"
    const val DEFAULT_VALUE = "--"
  }
}

data class CurrencyData(var fiatCurrencyCode: String = DEFAULT_VALUE,
                        var fiatCurrencySymbol: String = DEFAULT_VALUE,
                        var fiatValue: String = DEFAULT_VALUE, var appcCode: String = DEFAULT_VALUE,
                        var appcSymbol: String = DEFAULT_VALUE,
                        var appcValue: String = DEFAULT_VALUE) : Serializable
