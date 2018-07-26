package com.asfoundation.wallet.service;

import com.asfoundation.wallet.entity.ConvertToFiatResponseBody;
import com.asfoundation.wallet.entity.FiatValueRequest;
import com.asfoundation.wallet.ui.iab.FiatValue;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by franciscocalado on 24/07/2018.
 */

public class TokenToFiatService {

  private final TokenToFiatApi tokenToFiatApi;

  public TokenToFiatService(TokenToFiatApi tokenToFiatApi) {

    this.tokenToFiatApi = tokenToFiatApi;
  }

  public Observable<FiatValue> convertAppcToFiat(double value) {
    return tokenToFiatApi.convertAppcToFiat(new FiatValueRequest(value))
        .map(response -> mapToFiatValue(response))
        .subscribeOn(Schedulers.io());
  }

  private FiatValue mapToFiatValue(ConvertToFiatResponseBody responseBody) {
    return new FiatValue(responseBody.getAmount(), responseBody.getCurrency());
  }

  public interface TokenToFiatApi {
    @POST("/binance/rate") Observable<ConvertToFiatResponseBody> convertAppcToFiat(
        @Body FiatValueRequest request);
  }
}
