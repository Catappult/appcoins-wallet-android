package com.asfoundation.wallet.promotions.usecases

import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.appcoins.wallet.feature.changecurrency.data.currencies.LocalCurrencyConversionService
import com.appcoins.wallet.feature.changecurrency.data.use_cases.GetSelectedCurrencyUseCase
import com.appcoins.wallet.feature.walletInfo.data.balance.BalanceRepository
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
