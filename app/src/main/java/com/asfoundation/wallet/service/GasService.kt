package com.asfoundation.wallet.service

import com.asf.wallet.BuildConfig
import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import retrofit2.http.GET
import java.math.BigInteger

interface GasService {

  @GET("transaction/gas_price")
  fun getGasPrice(): Single<GasPrice>

  companion object {
    const val API_BASE_URL = BuildConfig.BACKEND_HOST
  }

}

data class GasPrice(@SerializedName("gas_price") val price: BigInteger)