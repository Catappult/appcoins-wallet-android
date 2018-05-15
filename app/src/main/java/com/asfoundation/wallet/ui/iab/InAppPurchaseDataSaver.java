package com.asfoundation.wallet.ui.iab;

import com.asfoundation.wallet.repository.Cache;
import com.asfoundation.wallet.repository.InAppPurchaseService;
import com.asfoundation.wallet.repository.PaymentTransaction;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;

public class InAppPurchaseDataSaver {
  private final InAppPurchaseService inAppPurchaseService;
  private final Cache<String, InAppPurchaseData> cache;
  private final Scheduler scheduler;
  private Disposable disposable;

  public InAppPurchaseDataSaver(InAppPurchaseService inAppPurchaseService,
      Cache<String, InAppPurchaseData> cache, Scheduler scheduler) {
    this.inAppPurchaseService = inAppPurchaseService;
    this.cache = cache;
    this.scheduler = scheduler;
  }

  public void start() {
    disposable = inAppPurchaseService.getAll()
        .subscribeOn(scheduler)
        .flatMapSingle(paymentTransactions -> Observable.fromIterable(paymentTransactions)
            .filter(paymentTransaction -> paymentTransaction.getState()
                .equals(PaymentTransaction.PaymentState.COMPLETED))
            .map(paymentTransaction -> new InAppPurchaseData(paymentTransaction.getBuyHash()))
            .doOnNext(inAppPurchaseData -> cache.saveSync(inAppPurchaseData.getTransactionId(),
                inAppPurchaseData))
            .toList())
        .subscribe();
  }

  public void stop() {
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
    }
    for (InAppPurchaseData inAppPurchaseData : cache.getAllSync()) {
      cache.removeSync(inAppPurchaseData.getTransactionId());
    }
  }
}

