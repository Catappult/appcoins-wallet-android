package com.appcoins.wallet.appcoins.rewards.repository.bds

import com.google.gson.annotations.SerializedName
import io.reactivex.Completable
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface BdsApi {
  @POST("broker/8.20180518/gateways/appcoins_rewards/transactions")
  fun pay(@Query("wallet.address") walletAddress: String, @Query("wallet.signature")
  signature: String, @Body payBody: PayBody): Completable

  data class PayBody(@SerializedName("price.value") private val amount: String,
                     private val origin: Origin,
                     private val product: String,
                     private val type: Type,
                     @SerializedName("wallets.developer") private val developerAddress: String,
                     @SerializedName("wallets.store") private val storeAddress: String,
                     @SerializedName("wallets.oem") private val oemAddress: String)
}

enum class Type {
  INAPP
}

enum class Origin {
  BDS, UNKNOWN
}
