package com.asfoundation.wallet.change_currency.use_cases

import com.appcoins.wallet.ui.arch.data.DataResult
import com.asfoundation.wallet.change_currency.FiatCurrenciesRepository
import javax.inject.Inject

class GetSelectedCurrencyUseCase @Inject constructor(
  private val fiatCurrenciesRepository: FiatCurrenciesRepository
) {

  suspend operator fun invoke(bypass: Boolean): DataResult<String> {
    return if (bypass) fiatCurrenciesRepository.getCachedSelectedCurrency()
    else fiatCurrenciesRepository.getSelectedCurrency()
  }
}