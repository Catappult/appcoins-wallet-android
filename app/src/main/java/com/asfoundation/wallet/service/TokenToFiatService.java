package com.asfoundation.wallet.service;

import com.asfoundation.wallet.entity.FiatValueRequest;
import com.asfoundation.wallet.entity.FiatValueResponse;
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

  public Observable<FiatValueResponse> convertAppcToFiat(double value) {
    return tokenToFiatApi.convertAppcToFiat(new FiatValueRequest(value))
        .subscribeOn(Schedulers.io());
  }

  public interface TokenToFiatApi {
    @POST("/binance/rate") Observable<FiatValueResponse> convertAppcToFiat(
        @Body FiatValueRequest request);
  }
}
