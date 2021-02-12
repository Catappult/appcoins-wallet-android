package com.asfoundation.wallet.promotions.voucher

import io.reactivex.Single
import java.util.*

class EVoucherDetailsInteractor {

  fun getDiamondModels(): Single<List<SkuButtonModel>> {
    val skuButtonModels: MutableList<SkuButtonModel> = LinkedList()
    skuButtonModels.add(
        SkuButtonModel("43_diamonds", "43 Diamonds", Price(0.99, "USD", "$", 24.81)))
    skuButtonModels.add(
        SkuButtonModel("218_diamonds", "218 Diamonds", Price(0.99, "USD", "$", 24.81)))
    skuButtonModels.add(
        SkuButtonModel("430_diamonds", "430 Diamonds", Price(0.99, "USD", "$", 24.81)))
    skuButtonModels.add(
        SkuButtonModel("43_diamonds", "43 Diamonds", Price(0.99, "USD", "$", 24.81)))
    skuButtonModels.add(
        SkuButtonModel("218_diamonds", "218 Diamonds", Price(0.99, "USD", "$", 24.81)))
    skuButtonModels.add(
        SkuButtonModel("430_diamonds", "430 Diamonds", Price(0.99, "USD", "$", 24.81)))
    return Single.just(skuButtonModels)
  }

  fun getTitle(): String {
    return "Voucher for Garena Free Fire: BOOYAH Day"
  }
}