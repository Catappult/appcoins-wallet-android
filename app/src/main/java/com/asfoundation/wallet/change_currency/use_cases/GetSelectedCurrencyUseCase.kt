package com.asfoundation.wallet.change_currency.use_cases

import com.asfoundation.wallet.change_currency.FiatCurrenciesRepository
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import io.reactivex.Single

class GetSelectedCurrencyUseCase(private val fiatCurrenciesRepository: FiatCurrenciesRepository,
                                 private val conversionService: LocalCurrencyConversionService) {

  operator fun invoke(): Single<String> {
    return conversionService.localCurrency.flatMap {
      return@flatMap fiatCurrenciesRepository.getSelectedCurrencyFirstTimeCheck(it.currency)
    }
  }
}