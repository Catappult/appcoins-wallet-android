package com.asfoundation.wallet.service

import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import retrofit2.http.GET
import java.math.BigInteger

interface GasService {

  @GET("transaction/gas_price")
  fun getGasPrice(): Single<GasPrice>
}

data class GasPrice(@SerializedName("gas_price") val price: BigInteger)