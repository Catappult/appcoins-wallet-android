package com.asfoundation.wallet.promotions.voucher

import com.asfoundation.wallet.vouchers.VouchersRepository
import io.reactivex.Single

class VoucherDetailsInteractor(val repository: VouchersRepository) {

  fun getVoucherSkus(packageName: String): Single<VoucherSkuModelList> {
    return repository.getVoucherSkuList(packageName)
  }
}