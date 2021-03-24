package com.asfoundation.wallet.repository

import com.asfoundation.wallet.service.LocalCurrencyConversionService
import com.asfoundation.wallet.service.TokenRateService
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Single

class CurrencyConversionService(private val tokenRateService: TokenRateService,
                                private val localCurrencyConversionService: LocalCurrencyConversionService) {
  fun getTokenValue(currency: String?): Single<FiatValue> = tokenRateService.getAppcRate(currency)

  fun getLocalFiatAmount(appcValue: String?): Single<FiatValue> =
      localCurrencyConversionService.getAppcToLocalFiat(appcValue, 18)
          .firstOrError()
}