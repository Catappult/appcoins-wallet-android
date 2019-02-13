package com.asfoundation.wallet.service;

import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.entity.AppcToLocalFiatResponseBody;
import com.asfoundation.wallet.ui.iab.FiatValue;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
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

  public Single<FiatValue> getAppcToLocalFiat(String appcValue) {
    return tokenToLocalFiatApi.getAppcToLocalFiat(appcValue)
        .map(appcToFiatResponseBody -> appcToFiatResponseBody)
        .map(response -> new FiatValue(
            new BigDecimal(response.getAppcValue()).setScale(2, RoundingMode.CEILING)
                .doubleValue(), response.getCurrency()))
        .subscribeOn(Schedulers.io())
        .singleOrError();
  }

  public interface TokenToLocalFiatApi {
    @GET("broker/8.20180518/exchanges/APPC/convert/{appcValue}")
    Observable<AppcToLocalFiatResponseBody> getAppcToLocalFiat(@Path("appcValue") String appcValue);
  }
}