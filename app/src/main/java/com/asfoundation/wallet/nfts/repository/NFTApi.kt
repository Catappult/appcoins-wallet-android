package com.asfoundation.wallet.nfts.repository
import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path


interface NftApi {
    @GET("/transaction/wallet/{wallet}/nfts")
    fun getNFTsOfWallet(@Path("wallet") owner: String): Single<List<NftAssetResponse>>
}

data class NftAssetResponse(
    @SerializedName("id") val id: String,
    @SerializedName("token_id") val tokenId: String,
    @SerializedName("image_preview_url") val imagePreviewUrl: String?,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("contract_address") val contractAddress: String

)