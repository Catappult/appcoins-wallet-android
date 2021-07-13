package com.asfoundation.wallet.change_currency

import com.asfoundation.wallet.service.currencies.FiatCurrenciesResponse

class FiatCurrenciesMapper {

  fun mapResponseToCurrencyList(response: FiatCurrenciesResponse): List<FiatCurrency> {
    val currencyList: MutableList<FiatCurrency> = ArrayList()
    for (currencyItem in response.items) {
      currencyList.add(
          FiatCurrency(currencyItem.currency!!, currencyItem.flag, currencyItem.label,
              currencyItem.sign))
    }
    return currencyList.toList()
  }
}