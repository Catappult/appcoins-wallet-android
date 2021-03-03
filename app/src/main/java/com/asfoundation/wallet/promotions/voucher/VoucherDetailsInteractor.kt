package com.asfoundation.wallet.promotions.voucher

import io.reactivex.Single
import java.util.*

class VoucherDetailsInteractor {

  fun getVoucherSkus(): Single<VoucherSkuModelList> {
    val voucherSkuModels: MutableList<VoucherSkuItem> = LinkedList()
    voucherSkuModels.add(
        VoucherSkuItem("43_diamonds", "43 Diamonds", Price(0.99, "USD", "$", 24.81)))
    voucherSkuModels.add(
        VoucherSkuItem("218_diamonds", "218 Diamonds", Price(0.99, "USD", "$", 24.81)))
    voucherSkuModels.add(
        VoucherSkuItem("430_diamonds", "430 Diamonds", Price(0.99, "USD", "$", 24.81)))
    voucherSkuModels.add(
        VoucherSkuItem("43_diamonds", "43 Diamonds", Price(0.99, "USD", "$", 24.81)))
    voucherSkuModels.add(
        VoucherSkuItem("218_diamonds", "218 Diamonds", Price(0.99, "USD", "$", 24.81)))
    voucherSkuModels.add(
        VoucherSkuItem("430_diamonds", "430 Diamonds", Price(0.99, "USD", "$", 24.81)))
    return Single.just(VoucherSkuModelList(voucherSkuModels))
  }
}