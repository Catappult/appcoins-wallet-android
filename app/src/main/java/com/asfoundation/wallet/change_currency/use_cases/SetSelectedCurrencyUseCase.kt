package com.asfoundation.wallet.change_currency.use_cases

import com.asfoundation.wallet.change_currency.FiatCurrenciesRepository
import io.reactivex.Completable
import javax.inject.Inject

class SetSelectedCurrencyUseCase @Inject constructor(
    private val fiatCurrenciesRepository: FiatCurrenciesRepository) {

  operator fun invoke(fiatCurrency: String): Completable {
    return Completable.fromAction {
      fiatCurrenciesRepository.setSelectedCurrency(fiatCurrency)
    }
  }
}