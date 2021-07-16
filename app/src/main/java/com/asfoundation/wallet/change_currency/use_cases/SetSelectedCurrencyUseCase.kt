package com.asfoundation.wallet.change_currency.use_cases

import com.asfoundation.wallet.change_currency.FiatCurrenciesRepository

class SetSelectedCurrencyUseCase(private val fiatCurrenciesRepository: FiatCurrenciesRepository) {

  operator fun invoke(fiatCurrency: String) {
    fiatCurrency.let { fiatCurrenciesRepository.setSelectedCurrency(it) }
  }
}