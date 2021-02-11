package com.asfoundation.wallet.promotions.voucher

class SkuButtonModel(val skuId: String, val title: String, val price: Price)

class Price(val value: Double, val currency: String, val symbol: String, val appc: Double)