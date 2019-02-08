package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.service.CurrencyConversionService;
import com.asfoundation.wallet.service.LocalCurrencyConversionService;
import com.asfoundation.wallet.ui.iab.FiatValue;
import io.reactivex.Single;

/**
 * Created by franciscocalado on 24/07/2018.
 */

public class ExpressCheckoutBuyService {

  private final CurrencyConversionService currencyConversionService;
  private final LocalCurrencyConversionService localCurrencyConversionService;

  public ExpressCheckoutBuyService(CurrencyConversionService currencyConversionService,
      LocalCurrencyConversionService localCurrencyConversionService) {
    this.currencyConversionService = currencyConversionService;
    this.localCurrencyConversionService = localCurrencyConversionService;
  }

  public Single<FiatValue> getTokenValue(String currency) {
    return currencyConversionService.getAppcRate(currency);
  }

  public Single<FiatValue> getLocalFiatAmount(String appcValue) {
    return localCurrencyConversionService.getAppcToLocalFiat(appcValue);
  }
}
