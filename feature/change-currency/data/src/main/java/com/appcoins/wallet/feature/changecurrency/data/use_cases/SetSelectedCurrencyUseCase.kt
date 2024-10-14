package com.appcoins.wallet.feature.changecurrency.data.use_cases

import com.appcoins.wallet.feature.changecurrency.data.FiatCurrenciesRepository
import javax.inject.Inject

class SetSelectedCurrencyUseCase @Inject constructor(
  private val fiatCurrenciesRepository: FiatCurrenciesRepository
) {

  suspend operator fun invoke(fiatCurrency: String) {
    fiatCurrenciesRepository.setSelectedCurrency(fiatCurrency)
  }
}