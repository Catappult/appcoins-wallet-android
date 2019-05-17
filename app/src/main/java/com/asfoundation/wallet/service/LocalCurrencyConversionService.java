package com.asfoundation.wallet.service;

import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.entity.ConversionResponseBody;
import com.asfoundation.wallet.ui.iab.FiatValue;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.math.BigDecimal;
import java.math.RoundingMode;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class LocalCurrencyConversionService {
  public static final String CONVERSION_HOST = BuildConfig.BASE_HOST;

  private final LocalCurrencyConversionService.TokenToLocalFiatApi tokenToLocalFiatApi;

  public LocalCurrencyConversionService(
      LocalCurrencyConversionService.TokenToLocalFiatApi tokenToLocalFiatApi) {

    this.tokenToLocalFiatApi = tokenToLocalFiatApi;
  }

  public Single<FiatValue> getLocalCurrency() {
    return getAppcToLocalFiat("1.0").firstOrError();
  }

  public Observable<FiatValue> getAppcToLocalFiat(String value) {
    return tokenToLocalFiatApi.getAppcToLocalFiat(value)
        .map(response -> new FiatValue(response.getAppcValue()
            .setScale(2, RoundingMode.CEILING), response.getCurrency(), response.getSymbol()));
  }

  public Observable<FiatValue> getCreditsToLocalFiat(String value) {
    return tokenToLocalFiatApi.getAppcToLocalFiat(value)
        .map(response -> new FiatValue(response.getAppcValue()
            .setScale(2, RoundingMode.CEILING), response.getCurrency(), response.getSymbol()));
  }

  public Observable<FiatValue> getEtherToLocalFiat(String value) {
    //TODO Mocked value while webservice is not up
    return Observable.just(new FiatValue(new BigDecimal(20), "EUR", "€"));
  }

  public Observable<FiatValue> getLocalToAppc(String currency, String value) {
    return tokenToLocalFiatApi.convertLocalToAppc(currency, value)
        .map(response -> new FiatValue(response.getAppcValue()
            .setScale(2, RoundingMode.CEILING), response.getCurrency(), response.getSymbol()));
  }

  public interface TokenToLocalFiatApi {
    @GET("broker/8.20180518/exchanges/APPC/convert/{appcValue}")
    Observable<ConversionResponseBody> getAppcToLocalFiat(@Path("appcValue") String appcValue);

    @GET("broker/8.20180518/exchanges/{localCurrency}/convert/{value}?to=APPC")
    Observable<ConversionResponseBody> convertLocalToAppc(@Path("localCurrency") String currency,
        @Path("value") String value);
  }
}