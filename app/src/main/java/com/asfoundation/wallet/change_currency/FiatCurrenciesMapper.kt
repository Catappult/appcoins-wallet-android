package com.asfoundation.wallet.change_currency

import com.asfoundation.wallet.service.currencies.FiatCurrenciesResponse

class FiatCurrenciesMapper {

  fun mapResponseToCurrencyList(response: FiatCurrenciesResponse): List<FiatCurrencyEntity> {
    return response.items.map {
      FiatCurrencyEntity(it.currency, it.flag, it.label, it.sign)
    }
  }
}