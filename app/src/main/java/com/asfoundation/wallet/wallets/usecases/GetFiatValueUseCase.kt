package com.asfoundation.wallet.wallets.usecases

import com.asfoundation.wallet.change_currency.use_cases.GetSelectedCurrencyUseCase
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Single

/**
 * Converts the input value in input currency to the selected fiat currency
 */
class GetFiatValueUseCase(private val getSelectedCurrencyUseCase: GetSelectedCurrencyUseCase,
                          private val localCurrencyConversionService: LocalCurrencyConversionService) {

  companion object {
    private const val SUM_FIAT_SCALE = 4
  }

  operator fun invoke(value: String, originalCurrency: String): Single<FiatValue> {
    return getSelectedCurrencyUseCase(bypass = false).flatMap { targetCurrency ->
          localCurrencyConversionService.getValueToFiat(value, originalCurrency, targetCurrency,
              SUM_FIAT_SCALE)
              .firstOrError()
        }
  }
}