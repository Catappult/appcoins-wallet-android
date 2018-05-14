package com.asfoundation.wallet.ui.iab;

import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.repository.InAppPurchaseService;
import com.asfoundation.wallet.repository.PaymentTransaction;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class InAppPurchaseInteractor {
  private final InAppPurchaseService inAppPurchaseService;

  public InAppPurchaseInteractor(InAppPurchaseService inAppPurchaseService) {
    this.inAppPurchaseService = inAppPurchaseService;
  }

  public Single<TransactionBuilder> parseTransaction(String uri) {
    return inAppPurchaseService.parseTransaction(uri);
  }

  public Completable send(String uri) {
    return inAppPurchaseService.send(uri);
  }

  public Observable<PaymentTransaction> getTransactionState(String uri) {
    return inAppPurchaseService.getTransactionState(uri);
  }

  public Completable remove(String uri) {
    return inAppPurchaseService.remove(uri);
  }
}
