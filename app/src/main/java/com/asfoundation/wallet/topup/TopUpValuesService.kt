package com.asfoundation.wallet.topup

import com.appcoins.wallet.core.network.microservices.api.product.TopUpValuesApi
import com.appcoins.wallet.core.utils.android_common.extensions.isNoNetworkException
import com.appcoins.wallet.sharedpreferences.FiatCurrenciesPreferencesDataSource
import com.asf.wallet.BuildConfig
import io.reactivex.Single
import javax.inject.Inject

class TopUpValuesService @Inject constructor(
  private val api: TopUpValuesApi,
  private val responseMapper: TopUpValuesApiResponseMapper,
  private val fiatCurrenciesPreferencesDataSource: FiatCurrenciesPreferencesDataSource
) {

  fun getDefaultValues(currency: String?): Single<TopUpValuesModel> {
    return api.getDefaultValues(
      packageName = BuildConfig.APPLICATION_ID,
      currency = currency ?: fiatCurrenciesPreferencesDataSource.getCachedSelectedCurrency()
    )
      .map { responseMapper.map(it) }
      .onErrorReturn { createErrorValuesList(it) }
  }

  fun getLimitValues(currency: String?, method: String?): Single<TopUpLimitValues> {
    return api.getInputLimitValues(
      packageName = BuildConfig.APPLICATION_ID,
      currency = currency ?: fiatCurrenciesPreferencesDataSource.getCachedSelectedCurrency(),
      method = method,
    )
      .map { responseMapper.mapValues(it) }
      .onErrorReturn { TopUpLimitValues(it.isNoNetworkException()) }
  }

  private fun createErrorValuesList(throwable: Throwable): TopUpValuesModel {
    return TopUpValuesModel(throwable.isNoNetworkException())
  }
}