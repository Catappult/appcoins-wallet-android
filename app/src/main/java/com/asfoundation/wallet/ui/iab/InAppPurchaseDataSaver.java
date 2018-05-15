package com.asfoundation.wallet.ui.iab;

import com.asfoundation.wallet.repository.Cache;
import com.asfoundation.wallet.repository.InAppPurchaseService;
import com.asfoundation.wallet.repository.PaymentTransaction;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import java.util.List;

public class InAppPurchaseDataSaver {
  private final InAppPurchaseService inAppPurchaseService;
  private final Cache<String, InAppPurchaseData> cache;
  private final AppInfoProvider appInfoProvider;
  private final Scheduler scheduler;
  private Disposable disposable;

  public InAppPurchaseDataSaver(InAppPurchaseService inAppPurchaseService,
      Cache<String, InAppPurchaseData> cache, AppInfoProvider appInfoProvider,
      Scheduler scheduler) {
    this.inAppPurchaseService = inAppPurchaseService;
    this.cache = cache;
    this.appInfoProvider = appInfoProvider;
    this.scheduler = scheduler;
  }

  public void start() {
    disposable = inAppPurchaseService.getAll()
        .subscribeOn(scheduler)
        .flatMapSingle(paymentTransactions -> Observable.fromIterable(paymentTransactions)
            .filter(paymentTransaction -> paymentTransaction.getState()
                .equals(PaymentTransaction.PaymentState.COMPLETED))
            .flatMap(paymentTransaction -> {
              InAppPurchaseData inAppPurchaseData =
                  appInfoProvider.get(paymentTransaction.getBuyHash(),
                      paymentTransaction.getPackageName(), paymentTransaction.getProductName());
              if (inAppPurchaseData == null) {
                return Observable.error(new IllegalArgumentException("app with the packageName "
                    + paymentTransaction.getPackageName()
                    + " does not exist"));
              }
              return Observable.just(inAppPurchaseData);
            })
            .doOnNext(inAppPurchaseData -> cache.saveSync(inAppPurchaseData.getTransactionId(),
                inAppPurchaseData))
            .toList())
        .doOnError(throwable -> throwable.printStackTrace())
        .retryWhen(this::isAppMissingError)
        .subscribe();
  }

  public Observable<Object> isAppMissingError(Observable<Throwable> throwableObservable) {
    return throwableObservable.flatMap(throwable -> {
      if (throwable instanceof IllegalArgumentException) {
        return Observable.empty();
      }
      return Observable.error(throwable);
    });
  }

  public Observable<InAppPurchaseData> get(String id) {
    return cache.get(id);
  }

  public void stop() {
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
    }
    for (InAppPurchaseData inAppPurchaseData : cache.getAllSync()) {
      cache.removeSync(inAppPurchaseData.getTransactionId());
    }
  }

  public Observable<List<InAppPurchaseData>> getAll() {
    return cache.getAll();
  }
}

