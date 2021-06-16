package com.asfoundation.wallet.repository

import com.asfoundation.wallet.service.TokenRateService
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Single

/**
 * Created by franciscocalado on 24/07/2018.
 */
class CurrencyConversionService(private val tokenRateService: TokenRateService,
                                private val localCurrencyConversionService: LocalCurrencyConversionService) {

  fun getTokenValue(currency: String?): Single<FiatValue> {
    return tokenRateService.getAppcRate(currency)
  }

  fun getLocalFiatAmount(appcValue: String): Single<FiatValue> {
    return localCurrencyConversionService.getAppcToLocalFiat(appcValue, 18)
        .firstOrError()
  }
}