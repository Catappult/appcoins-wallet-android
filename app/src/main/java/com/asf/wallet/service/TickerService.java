package com.asf.wallet.service;

import com.asf.wallet.entity.Ticker;
import com.asf.wallet.entity.Token;
import com.asf.wallet.entity.TokenTicker;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface TickerService {

  Observable<Ticker> fetchTickerPrice(String ticker);

  Single<TokenTicker[]> fetchTockenTickers(Token[] tokens, String currency);
}
