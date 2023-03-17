package com.asfoundation.wallet.topup

import com.appcoins.wallet.core.network.microservices.api.TopUpValuesApi
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.util.isNoNetworkException
import io.reactivex.Single
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
}