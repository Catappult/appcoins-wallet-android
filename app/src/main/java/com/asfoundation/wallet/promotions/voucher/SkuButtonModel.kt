package com.asfoundation.wallet.promotions.voucher

class SkuButtonModel(val skuId: String, val title: String, val price: Price,
                     val error: Boolean = false) {

  constructor() : this("", "", Price(0.0, "", "", 0.0), true)

}

class Price(val value: Double, val currency: String, val symbol: String, val appc: Double)