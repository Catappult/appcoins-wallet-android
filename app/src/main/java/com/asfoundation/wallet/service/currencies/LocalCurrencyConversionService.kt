package com.asfoundation.wallet.service.currencies

import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.entity.ConversionResponseBody
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import java.math.RoundingMode

class LocalCurrencyConversionService(
    private val tokenToLocalFiatApi: TokenToLocalFiatApi,
    private val currencyConversionRatesPersistence: CurrencyConversionRatesPersistence) {

  val localCurrency: Single<FiatValue>
    get() = getAppcToLocalFiat("1.0", 18).firstOrError()

  fun getAppcToLocalFiat(value: String, scale: Int,
                         getFromCache: Boolean = false): Observable<FiatValue> {
    return if (getFromCache) {
      currencyConversionRatesPersistence.getAppcToLocalFiat(value, scale)
          .toObservable()
    } else tokenToLocalFiatApi.getValueToLocalFiat(value, "APPC")
        .flatMap { response: ConversionResponseBody ->
          val convertedValue = FiatValue(response.appcValue
              .setScale(scale, RoundingMode.FLOOR), response.currency, response.symbol)
          currencyConversionRatesPersistence.saveRateFromAppcToFiat(value, response.appcValue
              .toString(), response.currency, response.symbol)
              .andThen(Observable.just(convertedValue))
              .onErrorReturn { throwable: Throwable ->
                throwable.printStackTrace()
                convertedValue
              }
        }
  }

  fun getEtherToLocalFiat(value: String, scale: Int): Observable<FiatValue> {
    return tokenToLocalFiatApi.getValueToLocalFiat(value, "ETH")
        .flatMap { response: ConversionResponseBody ->
          val convertedValue = FiatValue(response.appcValue
              .setScale(scale, RoundingMode.FLOOR), response.currency, response.symbol)
          currencyConversionRatesPersistence.saveRateFromEthToFiat(value, response.appcValue
              .toString(), response.currency, response.symbol)
              .andThen(Observable.just(convertedValue))
              .onErrorReturn { throwable: Throwable ->
                throwable.printStackTrace()
                convertedValue
              }
        }
  }

  fun getFiatToAppc(currency: String, value: String, scale: Int): Observable<FiatValue> {
    return tokenToLocalFiatApi.convertFiatToAppc(currency, value)
        .map { response: ConversionResponseBody ->
          FiatValue(response.appcValue
              .setScale(scale, RoundingMode.FLOOR), response.currency, response.symbol)
        }
  }

  fun getFiatToLocalFiat(currency: String, value: String, scale: Int): Observable<FiatValue> {
    return tokenToLocalFiatApi.getValueToLocalFiat(currency, value)
      .map { response: ConversionResponseBody ->
        FiatValue(response.appcValue
          .setScale(scale, RoundingMode.FLOOR), response.currency, response.symbol)
      }
  }

  interface TokenToLocalFiatApi {
    @GET("broker/8.20180518/exchanges/{currencyFrom}/convert/{value}")
    fun getValueToLocalFiat(@Path("value") appcValue: String,
                            @Path("currencyFrom")
                            currencyFrom: String): Observable<ConversionResponseBody>

    @GET("broker/8.20180518/exchanges/{fiatCurrency}/convert/{value}?to=APPC")
    fun convertFiatToAppc(@Path("fiatCurrency") currency: String,
                           @Path("value") value: String): Observable<ConversionResponseBody>

  }

  companion object {
    const val CONVERSION_HOST = BuildConfig.BASE_HOST
  }
}