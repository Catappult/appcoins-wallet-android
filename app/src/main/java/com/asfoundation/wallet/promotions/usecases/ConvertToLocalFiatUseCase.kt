package com.asfoundation.wallet.promotions.usecases

import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.appcoins.wallet.feature.walletInfo.data.BalanceRepository
import com.github.michaelbull.result.get
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject

class ConvertToLocalFiatUseCase @Inject constructor(
  private val getSelectedCurrencyUseCase: com.appcoins.wallet.feature.changecurrency.data.use_cases.GetSelectedCurrencyUseCase,
  private val localCurrencyConversionService: com.appcoins.wallet.feature.changecurrency.data.currencies.LocalCurrencyConversionService,
  private val dispatchers: Dispatchers
) {

  operator fun invoke(valueToConvert: String, originalCurrency: String): Single<com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue> {
    return rxSingle(dispatchers.io) { getSelectedCurrencyUseCase(bypass = false) }
      .flatMap { targetCurrency ->
        localCurrencyConversionService.getValueToFiat(
          valueToConvert, originalCurrency,
          targetCurrency.get(), com.appcoins.wallet.feature.walletInfo.data.BalanceRepository.FIAT_SCALE
        )
      }
  }
}
