package com.asfoundation.wallet.change_currency.use_cases

import com.asfoundation.wallet.change_currency.FiatCurrenciesRepository
import javax.inject.Inject

class SetSelectedCurrencyUseCase @Inject constructor(
  private val fiatCurrenciesRepository: FiatCurrenciesRepository
) {

  operator fun invoke(fiatCurrency: String) {
    fiatCurrenciesRepository.setSelectedCurrency(fiatCurrency)
  }
}