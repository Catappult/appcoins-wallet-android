package com.appcoins.wallet.feature.changecurrency.data.use_cases

import com.appcoins.wallet.feature.changecurrency.data.FiatCurrenciesRepository
import javax.inject.Inject

class GetCachedCurrencySymbolUseCase @Inject constructor(
  private val fiatCurrenciesRepository: FiatCurrenciesRepository
) {
  operator fun invoke() = fiatCurrenciesRepository.getCachedSelectedCurrencySymbol()
}