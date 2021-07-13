package com.asfoundation.wallet.change_currency

import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import io.reactivex.Single

class SelectedCurrencyInteract(private val fiatCurrenciesRepository: FiatCurrenciesRepository,
                               private val conversionService: LocalCurrencyConversionService) {

  fun getChangeFiatCurrencyModel(shouldCheckFirstTime: Boolean): Single<ChangeFiatCurrency> {
    val fiatCurrencyList: Single<List<FiatCurrency>> =
        if (shouldCheckFirstTime) fiatCurrenciesRepository.checkFirstTime() else fiatCurrenciesRepository.mapAndSaveFiatCurrency()
    return Single.zip(fiatCurrencyList,
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

  fun setSelectedCurrency(fiatCurrency: FiatCurrency) {
    fiatCurrency.currency.let { fiatCurrenciesRepository.setSelectedCurrency(it) }
  }
}