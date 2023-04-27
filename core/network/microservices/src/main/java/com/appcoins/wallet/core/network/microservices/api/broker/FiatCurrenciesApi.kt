package com.appcoins.wallet.core.network.microservices.api.broker

import com.appcoins.wallet.core.arch.data.Error
import com.appcoins.wallet.core.network.base.call_adapter.ApiResult
import com.appcoins.wallet.core.network.microservices.model.response.FiatCurrenciesResponse
import retrofit2.http.GET

interface FiatCurrenciesApi {
  @GET("8.20210201/currencies?type=FIAT&icon.height=128")
  suspend fun getFiatCurrencies(): ApiResult<FiatCurrenciesResponse, Error>
}