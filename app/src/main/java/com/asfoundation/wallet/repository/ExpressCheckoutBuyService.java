package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.service.CurrencyConversionService;
import com.asfoundation.wallet.ui.iab.FiatValue;
import io.reactivex.Single;

/**
 * Created by franciscocalado on 24/07/2018.
 */

public class ExpressCheckoutBuyService {

  private final CurrencyConversionService currencyConversionService;

  public ExpressCheckoutBuyService(CurrencyConversionService currencyConversionService) {
    this.currencyConversionService = currencyConversionService;
  }

  public Single<FiatValue> getTokenValue(String currency) {
    return currencyConversionService.getAppcRate(currency);
  }
}
