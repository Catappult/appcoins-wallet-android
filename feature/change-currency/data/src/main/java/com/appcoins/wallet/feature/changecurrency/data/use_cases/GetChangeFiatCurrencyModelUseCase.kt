package com.appcoins.wallet.feature.changecurrency.data.use_cases

import com.appcoins.wallet.feature.changecurrency.data.ChangeFiatCurrency
import com.appcoins.wallet.ui.arch.data.DataResult
import com.appcoins.wallet.ui.arch.data.toDataResult
import com.appcoins.wallet.feature.changecurrency.data.FiatCurrenciesRepository
import com.appcoins.wallet.feature.changecurrency.data.currencies.LocalCurrencyConversionService
import com.github.michaelbull.result.unwrap
import kotlinx.coroutines.rx2.await
import javax.inject.Inject

class GetChangeFiatCurrencyModelUseCase @Inject constructor(
  private val fiatCurrenciesRepository: FiatCurrenciesRepository,
  private val conversionService: LocalCurrencyConversionService
) {

  suspend operator fun invoke(): DataResult<ChangeFiatCurrency> {
    val selectedCurrency = fiatCurrenciesRepository.getSelectedCurrency().unwrap()
    val fiatCurrencyList = fiatCurrenciesRepository.getCurrenciesList().unwrap()
    val changeFiatCurrencyModel = ChangeFiatCurrency(fiatCurrencyList, selectedCurrency)
    return if (changeFiatCurrencyModel.selectedCurrency.isEmpty()) {
      val localCurrency = conversionService.localCurrency.await()
      changeFiatCurrencyModel.copy(selectedCurrency = localCurrency.currency).toDataResult()
    } else {
      changeFiatCurrencyModel.toDataResult()
    }
  }
}