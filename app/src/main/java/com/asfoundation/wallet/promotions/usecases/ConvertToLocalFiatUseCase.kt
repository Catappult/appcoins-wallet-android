package com.asfoundation.wallet.promotions.usecases

import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.asfoundation.wallet.change_currency.use_cases.GetSelectedCurrencyUseCase
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.wallets.repository.BalanceRepository
import io.reactivex.Single
import javax.inject.Inject

class ConvertToLocalFiatUseCase @Inject constructor(
  private val getSelectedCurrencyUseCase: GetSelectedCurrencyUseCase,
  private val localCurrencyConversionService: LocalCurrencyConversionService,
  private val rxSchedulers: RxSchedulers
) {

  operator fun invoke(valueToConvert: String, originalCurrency: String): Single<FiatValue> {
    return getSelectedCurrencyUseCase(bypass = false)
      .subscribeOn(rxSchedulers.io)
      .flatMap { targetCurrency ->
        localCurrencyConversionService.getValueToFiat(
          valueToConvert, originalCurrency,
          targetCurrency, BalanceRepository.FIAT_SCALE
        )
          .subscribeOn(rxSchedulers.io)
      }
  }
}
