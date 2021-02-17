package com.asfoundation.wallet.vouchers

import com.asfoundation.wallet.promotions.VoucherListModel
import io.reactivex.Single

interface VouchersRepository {

  fun getVouchers(): Single<VoucherListModel>
}
