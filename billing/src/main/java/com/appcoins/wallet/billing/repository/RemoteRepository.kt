package com.appcoins.wallet.billing.repository

import com.appcoins.wallet.billing.repository.entity.GetPackageResponse
import com.appcoins.wallet.billing.repository.entity.GetPurchasesResponse
import com.appcoins.wallet.billing.repository.entity.Purchase
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

  internal fun getPurchases(packageName: String,
                            walletAddress: String,
                            walletSignature: String,
                            type: BillingSupportedType): Single<List<Purchase>> {
    return api.getPurchases(packageName, walletAddress, walletSignature,
        type.name.toLowerCase()).map { response -> response.items }
  }

  interface BdsApi {
    @GET("inapp/8.20180518/packages/{packageName}")
    fun getPackage(@Path("packageName") packageName: String, @Query("type")
    type: String): Single<GetPackageResponse>

    @GET("inapp/8.20180518/packages/{packageName}/products/{skuId}/purchase")
    fun getPurchases(@Path("packageName") packageName: String,
                     @Query("wallet.address") walletAddress: String,
                     @Query("wallet.signature") walletSignature: String,
                     @Query("type") type: String): Single<GetPurchasesResponse>
  }

}
