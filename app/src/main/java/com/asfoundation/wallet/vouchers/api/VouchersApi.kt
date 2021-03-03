package com.asfoundation.wallet.vouchers.api

import io.reactivex.Single
import retrofit2.http.GET

interface VouchersApi {
  @GET("voucher/apps")
  fun getAppsWithAvailableVouchers(): Single<VoucherAppListResponse>

  @GET("voucher/apps/{packageName}/vouchers")
  fun getVouchersForPackage(packageName: String): Single<VoucherSkuListResponse>
}