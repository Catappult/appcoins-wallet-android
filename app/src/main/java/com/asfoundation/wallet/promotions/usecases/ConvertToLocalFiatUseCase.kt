package com.asfoundation.wallet.promotions.usecases

import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.asfoundation.wallet.change_currency.use_cases.GetSelectedCurrencyUseCase
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.wallets.repository.BalanceRepository
import com.github.michaelbull.result.get
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject

class ConvertToLocalFiatUseCase @Inject constructor(
  private val getSelectedCurrencyUseCase: GetSelectedCurrencyUseCase,
  private val localCurrencyConversionService: LocalCurrencyConversionService,
  private val dispatchers: Dispatchers
) {

  operator fun invoke(valueToConvert: String, originalCurrency: String): Single<FiatValue> {
    return rxSingle(dispatchers.io) { getSelectedCurrencyUseCase(bypass = false) }
      .flatMap { targetCurrency ->
        localCurrencyConversionService.getValueToFiat(
          valueToConvert, originalCurrency,
          targetCurrency.get(), BalanceRepository.FIAT_SCALE
        )
      }
  }
}
