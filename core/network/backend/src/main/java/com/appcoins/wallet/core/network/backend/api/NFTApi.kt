package com.appcoins.wallet.core.network.backend.api

import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import java.math.BigDecimal


interface NftApi {
  @GET("/transaction/wallet/{wallet}/nfts")
  fun getWalletNFTs(@Path("wallet") owner: String): Single<List<NftAssetResponse>>
}

data class NftAssetResponse(@SerializedName("id") val id: String,
                            @SerializedName("token_id") val tokenId: BigDecimal,
                            @SerializedName("image_preview_url") val imagePreviewUrl: String?,
                            @SerializedName("name") val name: String,
                            @SerializedName("description") val description: String,
                            @SerializedName("contract_address") val contractAddress: String,
                            @SerializedName("schema_name") val schema: String

)