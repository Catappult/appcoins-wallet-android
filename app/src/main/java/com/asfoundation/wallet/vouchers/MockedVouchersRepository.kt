package com.asfoundation.wallet.vouchers

import com.asfoundation.wallet.promotions.Voucher
import com.asfoundation.wallet.promotions.VoucherListModel
import io.reactivex.Single

class MockedVouchersRepository : VouchersRepository {

  override fun getVouchers(): Single<VoucherListModel> {
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
}