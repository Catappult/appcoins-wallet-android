package com.appcoins.wallet.billing.repository

import android.os.Build
import com.appcoins.wallet.billing.BuildConfig
import com.appcoins.wallet.billing.repository.entity.GetPackageResponse
import com.appcoins.wallet.billing.repository.entity.GetPurchasesResponse
import com.appcoins.wallet.billing.repository.entity.Purchase
import com.appcoins.wallet.billing.repository.entity.DetailsResponseBody
import com.appcoins.wallet.billing.repository.entity.Product
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

class RemoteRepository(private val api: BdsApi, val responseMapper: BdsApiResponseMapper) {
  companion object {
    const val BASE_HOST = "http://api-dev.blockchainds.com"//BuildConfig.BASE_HOST

  }

  internal fun isBillingSupported(packageName: String,
                                  type: BillingSupportedType): Single<Boolean> {
    return api.getPackage(packageName, type.name.toLowerCase()).map { responseMapper.map(it) }
  }

  fun getSkuDetails(packageName: String, skus: List<String>,
                    type: String): Single<List<Product>> {
    return api.getPackages(packageName, skus.joinToString(separator = ","))
        .map { responseMapper.map(it) }
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

    @GET("inapp/8.20180518/packages/{packageName}/products")
    fun getPackages(@Path("packageName") packageName: String,
                    @Query("names") names: String): Single<DetailsResponseBody>

    @GET("inapp/8.20180518/packages/{packageName}/purchases")
    fun getPurchases(@Path("packageName") packageName: String,
                     @Query("wallet.address") walletAddress: String,
                     @Query("wallet.signature") walletSignature: String,
                     @Query("type") type: String): Single<GetPurchasesResponse>


  }

}
