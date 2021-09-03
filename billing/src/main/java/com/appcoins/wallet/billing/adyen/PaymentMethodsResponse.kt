package com.appcoins.wallet.billing.adyen

import com.google.gson.JsonObject
import java.math.BigDecimal

data class PaymentMethodsResponse(val price: Price, val payment: JsonObject)

data class Price(val value: BigDecimal, val currency: String)
