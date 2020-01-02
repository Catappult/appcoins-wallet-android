package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import java.math.BigDecimal

data class PaymentMethodsResponse(val price: Price, val payment: PaymentMethodsApiResponse)

data class Price(val value: BigDecimal, val currency: String)
