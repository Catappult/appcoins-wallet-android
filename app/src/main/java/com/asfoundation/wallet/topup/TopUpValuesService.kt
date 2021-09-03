package com.asfoundation.wallet.topup

import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.util.isNoNetworkException
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

class TopUpValuesService(private val api: TopUpValuesApi,
                         private val responseMapper: TopUpValuesApiResponseMapper) {

  fun getDefaultValues(): Single<TopUpValuesModel> {
    return api.getDefaultValues(BuildConfig.APPLICATION_ID)
        .map { responseMapper.map(it) }
        .onErrorReturn { createErrorValuesList(it) }
  }

  fun getLimitValues(): Single<TopUpLimitValues> {
    return api.getInputLimitValues(BuildConfig.APPLICATION_ID)
        .map { responseMapper.mapValues(it) }
        .onErrorReturn { TopUpLimitValues(it.isNoNetworkException()) }
  }

  private fun createErrorValuesList(throwable: Throwable): TopUpValuesModel {
    return TopUpValuesModel(throwable.isNoNetworkException())
  }

  interface TopUpValuesApi {
    @GET("product/8.20180518/topup/billing/domains/{packageName}")
    fun getInputLimitValues(@Path("packageName")
                            packageName: String): Single<TopUpLimitValuesResponse>

    @GET("product/8.20200402/topup/billing/domains/{packageName}/skus")
    fun getDefaultValues(
        @Path("packageName") packageName: String): Single<TopUpDefaultValuesResponse>
  }
}