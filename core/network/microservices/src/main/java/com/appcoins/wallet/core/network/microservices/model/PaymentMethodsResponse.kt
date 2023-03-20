package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.JsonObject
import java.math.BigDecimal

data class PaymentMethodsResponse(val adyenPrice: AdyenPrice, val payment: JsonObject)

data class AdyenPrice(val value: BigDecimal, val currency: String)
