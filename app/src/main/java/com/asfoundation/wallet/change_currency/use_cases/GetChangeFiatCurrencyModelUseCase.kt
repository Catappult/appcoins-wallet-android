package com.asfoundation.wallet.change_currency.use_cases

import com.appcoins.wallet.ui.arch.data.DataResult
import com.appcoins.wallet.ui.arch.data.toDataResult
import com.asfoundation.wallet.change_currency.ChangeFiatCurrency
import com.asfoundation.wallet.change_currency.FiatCurrenciesRepository
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import com.github.michaelbull.result.get
import kotlinx.coroutines.rx2.await
import javax.inject.Inject

class GetChangeFiatCurrencyModelUseCase @Inject constructor(
  private val fiatCurrenciesRepository: FiatCurrenciesRepository,
  private val conversionService: LocalCurrencyConversionService
) {

  suspend operator fun invoke(): DataResult<ChangeFiatCurrency> {
    val selectedCurrency = fiatCurrenciesRepository.getSelectedCurrency().get()
    val fiatCurrencyList = fiatCurrenciesRepository.getCurrenciesList().get()
    val changeFiatCurrencyModel = ChangeFiatCurrency(fiatCurrencyList!!, selectedCurrency!!)
    return if (changeFiatCurrencyModel.selectedCurrency.isEmpty()) {
      val localCurrency = conversionService.localCurrency.await()
      changeFiatCurrencyModel.copy(selectedCurrency = localCurrency.currency).toDataResult()
    } else {
      changeFiatCurrencyModel.toDataResult()
    }
  }
}