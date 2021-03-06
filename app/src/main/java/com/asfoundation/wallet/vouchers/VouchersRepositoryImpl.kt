package com.asfoundation.wallet.vouchers

import com.asfoundation.wallet.promotions.VoucherListModel
import com.asfoundation.wallet.promotions.voucher.VoucherSkuModelList
import com.asfoundation.wallet.vouchers.api.VouchersApi
import io.reactivex.Single

class VouchersRepositoryImpl(private val api: VouchersApi,
                             private val mapper: VouchersResponseMapper) : VouchersRepository {

  override fun getAppsWithVouchers(): Single<VoucherListModel> {
    return api.getAppsWithAvailableVouchers()
        .map { response -> mapper.mapAppWithVouchers(response) }
        .onErrorReturn { throwable -> mapper.mapAppWithVouchersError(throwable) }
  }

  override fun getVoucherSkuList(packageName: String): Single<VoucherSkuModelList> {
    return api.getVouchersForPackage(packageName)
        .map { response -> mapper.mapVoucherSkuList(response) }
        .onErrorReturn { throwable -> mapper.mapVoucherSkuListError(throwable) }
  }
}