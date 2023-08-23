package com.appcoins.wallet.feature.changecurrency.data.currencies

import io.reactivex.Completable
import io.reactivex.Single

interface CurrencyConversionRatesPersistence {

  /**
   * rate from appc to fiat, meaning it's fiatValue / appcValue
   */
  fun saveRateFromAppcToFiat(appcValue: String, fiatValue: String, fiatCurrency: String,
                             fiatSymbol: String): Completable

  /**
   * rate from eth to fiat, meaning it's fiatValue / ethValue
   */
  fun saveRateFromEthToFiat(ethValue: String, fiatValue: String, fiatCurrency: String,
                            fiatSymbol: String): Completable

  /**
   * applies the rate in this method
   */
  fun getAppcToLocalFiat(appcValue: String, scale: Int): Single<FiatValue>

  /**
   * applies the rate in this method
   */
  fun getEthToLocalFiat(ethValue: String, scale: Int): Single<FiatValue>

}