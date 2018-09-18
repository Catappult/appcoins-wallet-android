package com.asfoundation.wallet.repository;

import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.List;

public interface BuyService {
  void start();

  Completable buy(String key, PaymentTransaction paymentTransaction);

  Observable<BdsBuyService.BuyTransaction> getBuy(String uri);

  Observable<List<BdsBuyService.BuyTransaction>> getAll();

  Completable remove(String key);
}
