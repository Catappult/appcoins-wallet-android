package com.asfoundation.wallet.service.currencies

import com.asfoundation.wallet.entity.ConversionResponseBody
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.math.RoundingMode
import javax.inject.Inject

class LocalCurrencyConversionService @Inject constructor(
    private val tokenToLocalFiatApi: TokenToLocalFiatApi,
    private val currencyConversionRatesPersistence: CurrencyConversionRatesPersistence) {

  val localCurrency: Single<FiatValue>
    get() = getAppcToLocalFiat("1.0", 18)

  fun getAppcToLocalFiat(value: String, scale: Int,
                         getFromCache: Boolean = false): Single<FiatValue> {
    return if (getFromCache) {
      currencyConversionRatesPersistence.getAppcToLocalFiat(value, scale)
    } else getValueToFiat(value, "APPC", null, scale)
        .flatMap {
          currencyConversionRatesPersistence.saveRateFromAppcToFiat(value, it.amount
              .toString(), it.currency, it.symbol)
              .andThen(Single.just(it))
              .onErrorReturn { throwable: Throwable ->
                throwable.printStackTrace()
                it
              }
        }
  }

  fun getEtherToLocalFiat(value: String, scale: Int): Single<FiatValue> {
    return getValueToFiat(value, "ETH", null, scale)
        .flatMap {
          currencyConversionRatesPersistence.saveRateFromEthToFiat(value, it.amount
              .toString(), it.currency, it.symbol)
              .andThen(Single.just(it))
              .onErrorReturn { throwable: Throwable ->
                throwable.printStackTrace()
                it
              }
        }
  }

  fun getFiatToAppc(currency: String, value: String, scale: Int): Single<FiatValue> {
    return tokenToLocalFiatApi.convertFiatToAppc(currency, value)
        .map { response: ConversionResponseBody ->
          FiatValue(response.appcValue
              .setScale(scale, RoundingMode.FLOOR), response.currency, response.symbol)
        }
  }

  fun getValueToFiat(value: String, currency: String, targetCurrency: String? = null,
                     scale: Int): Single<FiatValue> {
    val api = if (targetCurrency != null) tokenToLocalFiatApi.getValueToTargetFiat(currency, value,
        targetCurrency) else tokenToLocalFiatApi.getValueToTargetFiat(currency, value)
    return api.map { response: ConversionResponseBody ->
      FiatValue(response.appcValue
          .setScale(scale, RoundingMode.FLOOR), response.currency, response.symbol)
    }
  }

  fun getFiatToLocalFiat(currency: String, value: String, scale: Int): Single<FiatValue> {
    return tokenToLocalFiatApi.getValueToTargetFiat(currency, value)
        .map { response: ConversionResponseBody ->
          FiatValue(response.appcValue
              .setScale(scale, RoundingMode.FLOOR), response.currency, response.symbol)
        }
  }

  interface TokenToLocalFiatApi {
    @GET("broker/8.20180518/exchanges/{currency}/convert/{value}")
    fun getValueToTargetFiat(@Path("currency") currency: String,
                             @Path("value") value: String): Single<ConversionResponseBody>

    @GET("broker/8.20180518/exchanges/{currency}/convert/{value}")
    fun getValueToTargetFiat(@Path("currency") currency: String,
                             @Path("value") value: String,
                             @Query("to")
                             targetCurrency: String): Single<ConversionResponseBody>

    @GET("broker/8.20180518/exchanges/{currency}/convert/{value}?to=APPC")
    fun convertFiatToAppc(@Path("currency") currency: String,
                          @Path("value") value: String): Single<ConversionResponseBody>
  }
}