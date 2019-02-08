package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.service.EventCurrencyConversionService;
import com.asfoundation.wallet.service.LocalCurrencyConversionService;
import com.asfoundation.wallet.ui.iab.FiatValue;
import io.reactivex.Single;

/**
 * Created by franciscocalado on 24/07/2018.
 */

public class CurrencyConversionService {

  private final EventCurrencyConversionService eventCurrencyConversionService;
  private final LocalCurrencyConversionService localCurrencyConversionService;

  public CurrencyConversionService(EventCurrencyConversionService eventCurrencyConversionService,
      LocalCurrencyConversionService localCurrencyConversionService) {
    this.eventCurrencyConversionService = eventCurrencyConversionService;
    this.localCurrencyConversionService = localCurrencyConversionService;
  }

  public Single<FiatValue> getTokenValue(String currency) {
    return eventCurrencyConversionService.getAppcRate(currency);
  }

  public Single<FiatValue> getLocalFiatAmount(String appcValue) {
    return localCurrencyConversionService.getAppcToLocalFiat(appcValue);
  }
}
