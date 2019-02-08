package com.asfoundation.wallet.service;

import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.entity.AppcToEventFiatResponseBody;
import com.asfoundation.wallet.ui.iab.FiatValue;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by franciscocalado on 24/07/2018.
 */

public class EventCurrencyConversionService {
  public static final String CONVERSION_HOST = BuildConfig.BACKEND_HOST;

  private final TokenToFiatApi tokenToFiatApi;

  public EventCurrencyConversionService(TokenToFiatApi tokenToFiatApi) {

    this.tokenToFiatApi = tokenToFiatApi;
  }

  public Single<FiatValue> getAppcRate(String currency) {
    return tokenToFiatApi.getAppcToFiatRate(currency)
        .map(appcToEventFiatResponseBody -> appcToEventFiatResponseBody)
        .map(AppcToEventFiatResponseBody::getFiatValue)
        .map(value -> new FiatValue(value, currency))
        .subscribeOn(Schedulers.io())
        .singleOrError();
  }

  public interface TokenToFiatApi {
    @GET("appc/value") Observable<AppcToEventFiatResponseBody> getAppcToFiatRate(
        @Query("currency") String currency);
  }
}
