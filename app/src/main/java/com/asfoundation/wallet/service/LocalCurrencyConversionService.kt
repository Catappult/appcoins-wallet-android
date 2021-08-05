package com.asfoundation.wallet.service

import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.entity.ConversionResponseBody
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import java.math.RoundingMode

class LocalCurrencyConversionService(private val tokenToLocalFiatApi: TokenToLocalFiatApi) {

  companion object {
    const val CONVERSION_HOST = BuildConfig.BASE_HOST
  }

  fun getLocalCurrency(): Single<FiatValue> = getAppcToLocalFiat("1.0", 18).firstOrError()

  fun getAppcToLocalFiat(value: String?, scale: Int,
                         rounding: RoundingMode? = RoundingMode.FLOOR): Observable<FiatValue> {
    return tokenToLocalFiatApi.getValueToLocalFiat(value, "APPC")
        .map { response: ConversionResponseBody ->
          FiatValue(response.appcValue.setScale(scale, rounding), response.currency, response.symbol)
        }
  }

  fun getEtherToLocalFiat(value: String?, scale: Int,
                          rounding: RoundingMode? = RoundingMode.FLOOR): Observable<FiatValue> {
    return tokenToLocalFiatApi.getValueToLocalFiat(value, "ETH")
        .map { response: ConversionResponseBody ->
          FiatValue(response.appcValue.setScale(scale, rounding), response.currency, response.symbol)
        }
  }

  fun getLocalToAppc(currency: String?, value: String?, scale: Int,
                     rounding: RoundingMode? = RoundingMode.FLOOR): Observable<FiatValue> {
    return tokenToLocalFiatApi.convertLocalToAppc(currency, value)
        .map { response: ConversionResponseBody ->
          FiatValue(response.appcValue.setScale(scale, rounding), response.currency, response.symbol)
        }
  }

  fun getFiatToLocalFiat(value: String?, currency: String, scale: Int,
                     rounding: RoundingMode? = RoundingMode.FLOOR): Observable<FiatValue> {
    return tokenToLocalFiatApi.convertLocalToAppc(value, currency)
      .map { response: ConversionResponseBody ->
        FiatValue(response.appcValue.setScale(scale, rounding), response.currency, response.symbol)
      }
  }

  interface TokenToLocalFiatApi {
    @GET("broker/8.20180518/exchanges/{currencyFrom}/convert/{appcValue}")
    fun getValueToLocalFiat(@Path("value") appcValue: String?,
                            @Path("currencyFrom")
                            valueFrom: String?): Observable<ConversionResponseBody>

    @GET("broker/8.20180518/exchanges/{localCurrency}/convert/{value}?to=APPC")
    fun convertLocalToAppc(@Path("localCurrency") currency: String?,
                           @Path("value") value: String?): Observable<ConversionResponseBody>
  }
}