package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.GasPrice
import io.reactivex.Single
import retrofit2.http.GET

interface GasServiceApi {
  @GET("transaction/gas_price")
  fun getGasPrice(): Single<GasPrice>
}