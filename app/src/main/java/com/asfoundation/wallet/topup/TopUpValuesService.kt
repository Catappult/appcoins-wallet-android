package com.asfoundation.wallet.topup

import com.asf.wallet.BuildConfig
import com.appcoins.wallet.core.utils.common.extensions.isNoNetworkException
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import javax.inject.Inject

class TopUpValuesService @Inject constructor(private val api: TopUpValuesApi,
                                             private val responseMapper: TopUpValuesApiResponseMapper) {

  companion object {
    private const val API_VERSION ="8.20180518"
  }

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
    @GET("8.20180518/topup/billing/domains/{packageName}")
    fun getInputLimitValues(@Path("packageName")
                            packageName: String): Single<TopUpLimitValuesResponse>

    @GET("8.20200402/topup/billing/domains/{packageName}/skus")
    fun getDefaultValues(
        @Path("packageName") packageName: String): Single<TopUpDefaultValuesResponse>
  }
}