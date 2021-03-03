package com.asfoundation.wallet.promotions.voucher

data class VoucherSkuItem(val skuId: String, val title: String, val price: Price,
                          val error: Boolean = false) {

  constructor() : this("", "", Price(0.0, "", "", 0.0), true)
}

data class Price(val value: Double, val currency: String, val symbol: String, val appc: Double)