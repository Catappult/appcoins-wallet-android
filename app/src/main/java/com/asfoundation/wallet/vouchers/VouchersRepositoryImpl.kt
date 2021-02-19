package com.asfoundation.wallet.vouchers

import com.asfoundation.wallet.promotions.VoucherListModel
import com.asfoundation.wallet.vouchers.api.VouchersApi
import io.reactivex.Single

class VouchersRepositoryImpl(private val api: VouchersApi,
                             private val mapper: VouchersResponseMapper) : VouchersRepository {

  override fun getAppsWithVouchers(): Single<VoucherListModel> {
    return api.getAppsWithAvailableVouchers()
        .map { response -> mapper.mapAppWithVouchers(response) }
        .onErrorReturn { throwable -> mapper.mapAppWithVouchersError(throwable) }
  }
}