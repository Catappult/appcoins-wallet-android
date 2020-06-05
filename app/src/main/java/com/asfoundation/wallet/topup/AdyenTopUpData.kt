package com.asfoundation.wallet.topup

import java.io.Serializable
import java.math.BigDecimal

data class AdyenTopUpData(val fiatValue: String, val fiatCurrencyCode: String,
                          val selectedCurrencyType: String, val bonusValue: BigDecimal,
                          val fiatCurrencySymbol: String, val appcValue: String) : Serializable
