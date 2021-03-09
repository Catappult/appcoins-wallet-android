package com.asfoundation.wallet.vouchers

import com.asfoundation.wallet.promotions.VoucherListModel
import com.asfoundation.wallet.promotions.voucher.VoucherSkuModelList
import io.reactivex.Single

interface VouchersRepository {

  fun getVoucherApps(): Single<VoucherListModel>

  fun getVoucherSkuList(packageName: String): Single<VoucherSkuModelList>
}
