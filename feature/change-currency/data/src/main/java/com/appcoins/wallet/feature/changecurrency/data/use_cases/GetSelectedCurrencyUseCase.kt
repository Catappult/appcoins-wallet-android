package com.appcoins.wallet.feature.changecurrency.data.use_cases

import com.appcoins.wallet.core.arch.data.DataResult
import com.appcoins.wallet.feature.changecurrency.data.FiatCurrenciesRepository
import javax.inject.Inject

class GetSelectedCurrencyUseCase @Inject constructor(
  private val fiatCurrenciesRepository: FiatCurrenciesRepository
) {

  suspend operator fun invoke(bypass: Boolean): DataResult<String> {
    return if (bypass) fiatCurrenciesRepository.getCachedSelectedCurrency()
    else fiatCurrenciesRepository.getSelectedCurrency()
  }
}