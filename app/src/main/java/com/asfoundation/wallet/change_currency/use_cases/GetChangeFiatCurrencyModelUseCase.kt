package com.asfoundation.wallet.change_currency.use_cases

import com.asfoundation.wallet.change_currency.ChangeFiatCurrency
import com.asfoundation.wallet.change_currency.FiatCurrenciesRepository
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import io.reactivex.Single
import javax.inject.Inject

class GetChangeFiatCurrencyModelUseCase @Inject constructor(
    private val fiatCurrenciesRepository: FiatCurrenciesRepository,
    private val conversionService: LocalCurrencyConversionService) {

  operator fun invoke(): Single<ChangeFiatCurrency> {
    return Single.zip(fiatCurrenciesRepository.getCurrenciesList(),
        fiatCurrenciesRepository.getSelectedCurrency(),
        { list, selectedCurrency -> ChangeFiatCurrency(list, selectedCurrency) })
        .flatMap { changeFiatCurrencyModel ->
          if (changeFiatCurrencyModel.selectedCurrency.isEmpty()) {
            return@flatMap conversionService.localCurrency
                .map { localCurrency ->
                  changeFiatCurrencyModel.copy(selectedCurrency = localCurrency.currency)
                }
          }
          return@flatMap Single.just(changeFiatCurrencyModel)
        }
  }
}