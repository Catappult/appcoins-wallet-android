package com.asfoundation.wallet.billing.partners

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query


interface BdsPartnersApi {
  @GET("/roles/8.20180518/stores")
  fun getWallet(@Query("package.name") packageName: String): Single<GetWalletResponse>
}

data class GetWalletResponse(val items: List<Store>)

data class Store(val user: Data)

data class Data(val wallet_address: String)
