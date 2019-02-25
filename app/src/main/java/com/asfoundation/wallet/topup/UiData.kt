package com.asfoundation.wallet.topup

import com.asfoundation.wallet.topup.paymentMethods.PaymentMethodData

data class UiData(val methods: List<PaymentMethodData>, var currency: CurrencyData)

data class CurrencyData(val fiatCurrencyCode: String, val fiatCurrencySymbol: String,
                        var fiatValue: String, val appcCode: String,
                        val appcSymbol: String, var appcValue: String)
