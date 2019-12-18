package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.base.model.PaymentMethodsApiResponse

data class PaymentMethodsResponse(val price: Price, val payment: PaymentMethodsApiResponse)

data class Price(val value: String, val currency: String)
