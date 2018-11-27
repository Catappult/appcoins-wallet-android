package com.appcoins.wallet.appcoins.rewards.repository.bds

import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface BdsApi {
  @POST("broker/8.20180518/gateways/appcoins_credits/transactions")
  fun pay(@Query("wallet.address") walletAddress: String, @Query("wallet.signature")
  signature: String, @Body payBody: PayBody): Single<Transaction>

  data class PayBody(@SerializedName("price.value") private val amount: String,
                     private val origin: String?,
                     private val product: String?,
                     private val type: String,
                     @SerializedName("metadata") private val payload: String?,
                     @SerializedName("callback_url") private val callback: String?,
                     @SerializedName("wallets.developer") private val developerAddress: String,
                     @SerializedName("wallets.store") private val storeAddress: String,
                     @SerializedName("wallets.oem") private val oemAddress: String,
                     @SerializedName("price.currency") private val currency: String,
                     @SerializedName("domain") private val packageName: String)
}