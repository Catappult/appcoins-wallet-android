package com.appcoins.wallet.billing.repository

import com.appcoins.wallet.billing.repository.entity.Product
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

class RemoteRepository(private val api: BdsApi) {
  companion object {
    const val BASE_HOST = "https://api.blockchainds.com"
  }

  internal fun isBillingSupported(packageName: String,
                                  type: BillingSupportedType): Single<Boolean> {
    return api.getPackage(packageName, type.name.toLowerCase()).map { true }
  }

  fun getSkuDetails(packageName: String, sku: String,
                    type: String,
                    walletAddress: String,
                    walletSignature: String): Single<Product> {
    return api.getSkuDetails(packageName, sku, walletAddress, walletSignature)
  }

  interface BdsApi {
    @GET("inapp/8.20180518/packages/{packageName}")
    fun getPackage(@Path("packageName") packageName: String, @Query("type")
    type: String): Single<GetPackageResponse>

    @GET("packages/{packageName}/products/{sku}/purchase")
    fun getSkuDetails(@Path("packageName") packageName: String, @Path("sku") sku: String,
                      @Query("wallet.address") walletAddress: String,
                      @Query("wallet.signature") walletSignature: String): Single<Product>

  }

  data class GetPackageResponse(val name: String)
}
