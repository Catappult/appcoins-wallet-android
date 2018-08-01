package com.asfoundation.wallet.service;

import com.asfoundation.wallet.entity.ConvertToFiatResponseBody;
import com.asfoundation.wallet.ui.iab.FiatValue;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by franciscocalado on 24/07/2018.
 */

public class TokenToFiatService {
  public static final String TOKEN_TO_FIAT_END_POINT = "http://34.254.1.70";

  private final TokenToFiatApi tokenToFiatApi;

  public TokenToFiatService(TokenToFiatApi tokenToFiatApi) {

    this.tokenToFiatApi = tokenToFiatApi;
  }

  public Single<FiatValue> convertAppcToFiat(double value) {
    return tokenToFiatApi.convertAppcToFiat(value)
        .map(responseBody -> mapToFiatValue(responseBody))
        .subscribeOn(Schedulers.io());
  }

  private FiatValue mapToFiatValue(ConvertToFiatResponseBody responseBody) {
    return new FiatValue(responseBody.getAmount(), responseBody.getCurrency());
  }

  public interface TokenToFiatApi {
    @GET("/appc/rate") Single<ConvertToFiatResponseBody> convertAppcToFiat(
        @Query("appc") double value);
  }
}
