package com.appcoins.wallet.bdsbilling.subscriptions

import java.math.BigDecimal

data class OrderResponse(val gateway: String, val reference: String, val value: BigDecimal,
                         val label: String, val currency: String, val symbol: String,
                         val created: String, val method: MethodResponse, val appc: AppcPrice)

data class MethodResponse(val title: String, val icon: String)

data class AppcPrice(val value: BigDecimal, val label: String)