package com.asfoundation.wallet.billing.partners

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query


interface BdsPartnersApi {
  @GET("/roles/8.20180518/stores")
  fun getStoreWallet(@Query("package.name") packageName: String): Single<GetWalletResponse>

  @GET("/roles/8.20180518/oems")
  fun getOemWallet(@Query("package.name") packageName: String,
                   @Query("device.manufacturer") manufacturer: String?,
                   @Query("device.model") model: String?): Single<GetWalletResponse>
}

data class GetWalletResponse(val items: List<Store>)

data class Store(val user: Data)

data class Data(val wallet_address: String)
