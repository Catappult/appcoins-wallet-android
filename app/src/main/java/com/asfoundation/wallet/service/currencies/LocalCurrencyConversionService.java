package com.asfoundation.wallet.service.currencies;

import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.entity.ConversionResponseBody;
import com.asfoundation.wallet.ui.iab.FiatValue;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.math.RoundingMode;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class LocalCurrencyConversionService {
  public static final String CONVERSION_HOST = BuildConfig.BASE_HOST;

  private final LocalCurrencyConversionService.TokenToLocalFiatApi tokenToLocalFiatApi;
  private final CurrencyConversionRatesPersistence currencyConversionRatesPersistence;

  public LocalCurrencyConversionService(
      LocalCurrencyConversionService.TokenToLocalFiatApi tokenToLocalFiatApi,
      CurrencyConversionRatesPersistence currencyConversionRatesPersistence) {

    this.tokenToLocalFiatApi = tokenToLocalFiatApi;
    this.currencyConversionRatesPersistence = currencyConversionRatesPersistence;
  }

  public Single<FiatValue> getLocalCurrency() {
    return getAppcToLocalFiat("1.0", 18, false).firstOrError();
  }

  public Observable<FiatValue> getAppcToLocalFiat(String value, int scale, boolean getFromCache) {
    if (getFromCache) {
      return currencyConversionRatesPersistence.getAppcToLocalFiat(value, scale)
          .toObservable();
    }
    return tokenToLocalFiatApi.getValueToLocalFiat(value, "APPC")
        .flatMap(response -> {
          FiatValue convertedValue = new FiatValue(response.getAppcValue()
              .setScale(scale, RoundingMode.FLOOR), response.getCurrency(), response.getSymbol());
          return currencyConversionRatesPersistence.saveRateFromAppcToFiat(value, response.getAppcValue()
              .toString(), response.getCurrency(), response.getSymbol())
              .andThen(Observable.just(convertedValue))
              .onErrorReturn(throwable -> {
                throwable.printStackTrace();
                return convertedValue;
              });
        });
  }

  public Observable<FiatValue> getEtherToLocalFiat(String value, int scale) {
    return tokenToLocalFiatApi.getValueToLocalFiat(value, "ETH")
        .flatMap(response -> {
          FiatValue convertedValue = new FiatValue(response.getAppcValue()
              .setScale(scale, RoundingMode.FLOOR), response.getCurrency(), response.getSymbol());
          return currencyConversionRatesPersistence.saveRateFromEthToFiat(value, response.getAppcValue()
              .toString(), response.getCurrency(), response.getSymbol())
              .andThen(Observable.just(convertedValue))
              .onErrorReturn(throwable -> {
                throwable.printStackTrace();
                return convertedValue;
              });
        });
  }

  public Observable<FiatValue> getLocalToAppc(String currency, String value, int scale) {
    return tokenToLocalFiatApi.convertLocalToAppc(currency, value)
        .map(response -> new FiatValue(response.getAppcValue()
            .setScale(scale, RoundingMode.FLOOR), response.getCurrency(), response.getSymbol()));
  }

  public interface TokenToLocalFiatApi {
    @GET("broker/8.20180518/exchanges/{valueFrom}/convert/{appcValue}")
    Observable<ConversionResponseBody> getValueToLocalFiat(@Path("appcValue") String appcValue,
        @Path("valueFrom") String valueFrom);

    @GET("broker/8.20180518/exchanges/{localCurrency}/convert/{value}?to=APPC")
    Observable<ConversionResponseBody> convertLocalToAppc(@Path("localCurrency") String currency,
        @Path("value") String value);
  }
}