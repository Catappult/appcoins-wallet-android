package com.asfoundation.wallet.promotions

import com.asfoundation.wallet.promotions.voucher.Price
import com.asfoundation.wallet.promotions.voucher.VoucherSkuItem
import com.asfoundation.wallet.promotions.voucher.VoucherSkuModelList
import com.asfoundation.wallet.vouchers.VouchersRepository
import io.reactivex.Single

class MockedVouchersRepository :
    VouchersRepository {

  override fun getAppsWithVouchers(): Single<VoucherListModel> {
    return Single.just(VoucherListModel(listOf(
        Voucher("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        Voucher("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", false),
        Voucher("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        Voucher("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", false),
        Voucher("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        Voucher("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png",
            true))))
  }

  override fun getVoucherSkuList(packageName: String): Single<VoucherSkuModelList> {
    return Single.just(VoucherSkuModelList(listOf(
        VoucherSkuItem("100_diamonds", "100 Diamonds", Price(1.0, "USD", "$", 14.0)),
        VoucherSkuItem("200_diamonds", "200 Diamonds", Price(2.0, "USD", "$", 28.0)),
        VoucherSkuItem("300_diamonds", "300 Diamonds", Price(3.0, "USD", "$", 42.0)),
        VoucherSkuItem("400_diamonds", "400 Diamonds", Price(4.0, "USD", "$", 56.0)),
        VoucherSkuItem("500_diamonds", "500 Diamonds", Price(5.0, "USD", "$", 70.0))
    )))
  }
}