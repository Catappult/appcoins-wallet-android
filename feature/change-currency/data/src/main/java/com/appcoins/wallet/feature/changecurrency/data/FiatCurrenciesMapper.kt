package com.appcoins.wallet.feature.changecurrency.data

import com.appcoins.wallet.core.network.microservices.model.response.FiatCurrenciesResponse
import javax.inject.Inject

class FiatCurrenciesMapper @Inject constructor(){

  fun mapResponseToCurrencyList(response: FiatCurrenciesResponse): List<FiatCurrencyEntity> {
    return response.items.map {
      FiatCurrencyEntity(it.currency, it.flag, it.label, it.sign)
    }
  }
}