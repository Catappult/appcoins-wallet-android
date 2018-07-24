package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.FiatValueResponse;
import com.asfoundation.wallet.service.TokenToFiatService;
import io.reactivex.Observable;

/**
 * Created by franciscocalado on 24/07/2018.
 */

public class ExpressCheckoutBuyService {

  private final TokenToFiatService tokenToFiatService;

  public ExpressCheckoutBuyService(TokenToFiatService tokenToFiatService) {
    this.tokenToFiatService = tokenToFiatService;
  }

  public Observable<FiatValueResponse> getTokenValue(double value) {
    return tokenToFiatService.convertAppcToFiat(value);
  }
}
