package com.asfoundation.wallet.repository

import com.asfoundation.wallet.service.TokenRateService
import com.appcoins.wallet.feature.changecurrency.data.currencies.LocalCurrencyConversionService
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import io.reactivex.Single
import javax.inject.Inject

class CurrencyConversionService @Inject constructor(private val tokenRateService: TokenRateService,
                                private val localCurrencyConversionService: LocalCurrencyConversionService) {

  fun getTokenValue(currency: String?): Single<FiatValue> {
    return tokenRateService.getAppcRate(currency)
  }

  fun getLocalFiatAmount(appcValue: String): Single<FiatValue> {
    return localCurrencyConversionService.getAppcToLocalFiat(appcValue, 18)
  }

  fun getLocalFiatAmount(value: String, currency: String): Single<FiatValue> {
    return localCurrencyConversionService.getFiatToLocalFiat(currency, value, 2)
  }

  fun getFiatToAppcAmount(value: String, currency: String): Single<FiatValue> {
    return localCurrencyConversionService.getFiatToAppc(currency, value, 18)
  }
}