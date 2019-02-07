package com.asfoundation.wallet.service;

import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.entity.AppcToLocalFiatResponseBody;
import com.asfoundation.wallet.ui.iab.FiatValue;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class LocalCurrencyConversionService {
  public static final String CONVERSION_HOST = BuildConfig.BACKEND_HOST;

  private final LocalCurrencyConversionService.TokenToLocalFiatApi tokenToLocalFiatApi;

  public LocalCurrencyConversionService(
      LocalCurrencyConversionService.TokenToLocalFiatApi tokenToLocalFiatApi) {

    this.tokenToLocalFiatApi = tokenToLocalFiatApi;
  }

  public Single<FiatValue> getAppcToLocalFiat(String appcValue) {
    return tokenToLocalFiatApi.getAppcToLocalFiat(appcValue)
        .map(appcToFiatResponseBody -> appcToFiatResponseBody)
        .map(response -> new FiatValue(response.getAppcValue(), response.getCurrency()))
        .subscribeOn(Schedulers.io())
        .singleOrError();
  }

  public interface TokenToLocalFiatApi {
    @GET("broker/8.20180518/exchanges/APPC/convert/{appcValue}")
    Observable<AppcToLocalFiatResponseBody> getAppcToLocalFiat(@Path("appcValue") String appcValue);
  }
}