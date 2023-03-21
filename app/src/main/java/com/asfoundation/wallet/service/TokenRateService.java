package com.asfoundation.wallet.service;

import com.appcoins.wallet.core.network.backend.api.TokenToFiatApi;
import com.appcoins.wallet.core.network.backend.model.AppcToFiatResponseBody;
import com.asfoundation.wallet.ui.iab.FiatValue;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;

/**
 * Created by franciscocalado on 24/07/2018.
 */

public class TokenRateService {

  private final TokenToFiatApi tokenToFiatApi;

  public @Inject TokenRateService(TokenToFiatApi tokenToFiatApi) {
    this.tokenToFiatApi = tokenToFiatApi;
  }

  public Single<FiatValue> getAppcRate(String currency) {
    return tokenToFiatApi.getAppcToFiatRate(currency)
        .map(appcToFiatResponseBody -> appcToFiatResponseBody)
        .map(AppcToFiatResponseBody::getAppcValue)
        .map(value -> new FiatValue(value, currency, ""))
        .subscribeOn(Schedulers.io())
        .singleOrError();
  }
}
