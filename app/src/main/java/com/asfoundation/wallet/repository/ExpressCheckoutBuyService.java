package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.service.TokenToFiatService;
import com.asfoundation.wallet.ui.iab.FiatValue;
import io.reactivex.Single;

/**
 * Created by franciscocalado on 24/07/2018.
 */

public class ExpressCheckoutBuyService {

  private final TokenToFiatService tokenToFiatService;

  public ExpressCheckoutBuyService(TokenToFiatService tokenToFiatService) {
    this.tokenToFiatService = tokenToFiatService;
  }

  public Single<FiatValue> getTokenValue(String currency) {
    return tokenToFiatService.convertAppcToFiat(currency);
  }
}
