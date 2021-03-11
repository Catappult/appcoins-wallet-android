package com.asfoundation.wallet.vouchers.api

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface VouchersApi {
  @GET("voucher/apps")
  fun getAppsWithAvailableVouchers(): Single<VoucherAppListResponse>

  @GET("voucher/apps/{packageName}/vouchers")
  fun getVouchersForPackage(
      @Path("packageName") packageName: String): Single<VoucherSkuListResponse>
}