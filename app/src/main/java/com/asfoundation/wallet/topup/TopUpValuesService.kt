package com.asfoundation.wallet.topup

import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

class TopUpValuesService(private val api: TopUpValuesApi,
                         private val responseMapper: TopUpValuesApiResponseMapper) {

  fun getDefaultValues(): Single<List<FiatValue>> {
    return api.getDefaultValues(BuildConfig.APPLICATION_ID)
        .map { responseMapper.map(it) }
  }

  fun getMinimumValue(): Single<TopUpLimitValue> {
    return api.getInputLimitValues(BuildConfig.APPLICATION_ID)
        .map { responseMapper.mapMin(it) }
  }

  fun getMaximumValue(): Single<TopUpLimitValue> {
    return api.getInputLimitValues(BuildConfig.APPLICATION_ID)
        .map { responseMapper.mapMax(it) }
  }

  interface TopUpValuesApi {
    @GET("product/8.20180518/topup/billing/domains/{packageName}")
    fun getInputLimitValues(@Path("packageName")
                            packageName: String): Single<TopUpLimitValuesResponse>

    @GET("product/8.20180518/topup/billing/domains/{packageName}/skus")
    fun getDefaultValues(@Path("packageName")
                         packageName: String): Single<TopUpDefaultValuesResponse>
  }
}