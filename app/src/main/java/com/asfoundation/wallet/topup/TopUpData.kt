package com.asfoundation.wallet.topup

import com.asfoundation.wallet.topup.paymentMethods.PaymentMethodData
import java.io.Serializable

data class TopUpData(val methods: List<PaymentMethodData>, var currency: CurrencyData) :
    Serializable

data class CurrencyData(val fiatCurrencyCode: String, val fiatCurrencySymbol: String,
                        var fiatValue: String, val appcCode: String,
                        val appcSymbol: String, var appcValue: String)
