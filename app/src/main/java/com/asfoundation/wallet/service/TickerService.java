package com.asfoundation.wallet.service;

import com.asfoundation.wallet.entity.Ticker;
import com.asfoundation.wallet.entity.Token;
import com.asfoundation.wallet.entity.TokenTicker;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface TickerService {

  Observable<Ticker> fetchTickerPrice(String ticker);

  Single<TokenTicker[]> fetchTockenTickers(Token[] tokens, String currency);
}
