package com.asfoundation.wallet.change_currency.use_cases

import com.asfoundation.wallet.change_currency.FiatCurrenciesRepository
import io.reactivex.Single
import javax.inject.Inject

class GetSelectedCurrencyUseCase @Inject constructor(
    private val fiatCurrenciesRepository: FiatCurrenciesRepository) {

  operator fun invoke(bypass: Boolean): Single<String> {
    return if (bypass) fiatCurrenciesRepository.getCachedSelectedCurrency() else fiatCurrenciesRepository.getSelectedCurrency()
  }
}