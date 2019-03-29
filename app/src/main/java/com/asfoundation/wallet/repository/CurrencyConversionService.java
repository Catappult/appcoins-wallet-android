package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.service.TokenRateService;
import com.asfoundation.wallet.service.LocalCurrencyConversionService;
import com.asfoundation.wallet.ui.iab.FiatValue;
import io.reactivex.Single;

/**
 * Created by franciscocalado on 24/07/2018.
 */

public class CurrencyConversionService {

  private final TokenRateService tokenRateService;
  private final LocalCurrencyConversionService localCurrencyConversionService;

  public CurrencyConversionService(TokenRateService tokenRateService,
      LocalCurrencyConversionService localCurrencyConversionService) {
    this.tokenRateService = tokenRateService;
    this.localCurrencyConversionService = localCurrencyConversionService;
  }

  public Single<FiatValue> getTokenValue(String currency) {
    return tokenRateService.getAppcRate(currency);
  }

  public Single<FiatValue> getLocalFiatAmount(String appcValue) {
    return localCurrencyConversionService.getAppcToLocalFiat(appcValue).firstOrError();
  }
}
