package com.asfoundation.wallet.service;

import com.asfoundation.wallet.entity.AppcToFiatResponseBody;
import com.asfoundation.wallet.ui.iab.FiatValue;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by franciscocalado on 24/07/2018.
 */

public class TokenToFiatService {
  public static final String TOKEN_TO_FIAT_END_POINT = "http://34.254.1.70/";

  private final TokenToFiatApi tokenToFiatApi;

  public TokenToFiatService(TokenToFiatApi tokenToFiatApi) {

    this.tokenToFiatApi = tokenToFiatApi;
  }

  public Single<FiatValue> convertAppcToFiat(String currency) {
    return tokenToFiatApi.getAppcToFiatRate(currency)
        .map(appcToFiatResponseBody -> appcToFiatResponseBody)
        .map(AppcToFiatResponseBody::getFiatValue)
        .map(value -> new FiatValue(value, currency))
        .subscribeOn(Schedulers.io())
        .singleOrError();
  }

  public interface TokenToFiatApi {
    @GET("appc/value") Observable<AppcToFiatResponseBody> getAppcToFiatRate(
        @Query("currency") String currency);
  }
}
