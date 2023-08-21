package com.appcoins.wallet.feature.changecurrency.data

import com.appcoins.wallet.core.network.microservices.model.response.FiatCurrenciesResponse
import javax.inject.Inject

fun FiatCurrenciesResponse.mapResponseToCurrencyListEntity(): List<FiatCurrencyEntity> {
  return this.items.map {
    FiatCurrencyEntity(it.currency, it.flag, it.label, it.sign)
  }
}

fun List<FiatCurrencyEntity>.mapToCurrency(): List<FiatCurrency> {
  return this.map {
    FiatCurrency(it.currency, it.flag, it.label, it.sign)
  }
}