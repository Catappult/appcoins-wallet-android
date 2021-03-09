package com.asfoundation.wallet.vouchers.api

data class VoucherSkuResponse(val sku: String, val title: String, val price: Price)

data class Price(val value: String, val currency: String, val symbol: String, val appc: String)