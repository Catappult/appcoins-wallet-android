package com.asfoundation.wallet.promotions.voucher

import java.util.*

class EVoucherDetailsInteractor {

  fun getDiamondModels(): List<SkuButtonModel> {
    val skuButtonModels: MutableList<SkuButtonModel> = LinkedList()
    skuButtonModels.add(SkuButtonModel("43 Diamonds"))
    skuButtonModels.add(SkuButtonModel("218 Diamonds"))
    skuButtonModels.add(SkuButtonModel("430 Diamonds"))
    skuButtonModels.add(SkuButtonModel("43 Diamonds"))
    skuButtonModels.add(SkuButtonModel("218 Diamonds"))
    skuButtonModels.add(SkuButtonModel("430 Diamonds"))
    return skuButtonModels
  }

  fun getTitle(): String {
    return "Voucher for Garena Free Fire: BOOYAH Day"
  }
}