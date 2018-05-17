package com.asfoundation.wallet.ui.iab;

import com.asfoundation.wallet.poa.ProofOfAttentionService;
import com.asfoundation.wallet.poa.ProofStatus;
import com.asfoundation.wallet.repository.InAppPurchaseService;
import com.asfoundation.wallet.repository.PaymentTransaction;
import com.asfoundation.wallet.repository.Repository;
import com.asfoundation.wallet.ui.iab.database.AppCoinsOperation;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import java.util.List;

public class AppcoinsOperationsDataSaver {
  private static final String TAG = AppcoinsOperationsDataSaver.class.getSimpleName();
  private final InAppPurchaseService inAppPurchaseService;
  private final ProofOfAttentionService proofOfAttentionService;
  private final Repository<String, AppCoinsOperation> cache;
  private final AppInfoProvider appInfoProvider;
  private final Scheduler scheduler;
  private final CompositeDisposable disposables;

  public AppcoinsOperationsDataSaver(InAppPurchaseService inAppPurchaseService,
      ProofOfAttentionService proofOfAttentionService, Repository<String, AppCoinsOperation> cache,
      AppInfoProvider appInfoProvider, Scheduler scheduler, CompositeDisposable disposables) {
    this.inAppPurchaseService = inAppPurchaseService;
    this.proofOfAttentionService = proofOfAttentionService;
    this.cache = cache;
    this.appInfoProvider = appInfoProvider;
    this.scheduler = scheduler;
    this.disposables = disposables;
  }

  public void start() {
    disposables.add(Observable.merge(getInAppData(), getProofAppData())
        .doOnNext(inAppPurchaseData -> cache.saveSync(inAppPurchaseData.getTransactionId(),
            inAppPurchaseData))
        .doOnError(throwable -> throwable.printStackTrace())
        .retryWhen(this::isKnownErrorError)
        .subscribe());
  }

  private Observable<AppCoinsOperation> getInAppData() {
    return inAppPurchaseService.getAll()
        .subscribeOn(scheduler)
        .flatMap(paymentTransactions -> Observable.fromIterable(paymentTransactions)
            .filter(paymentTransaction -> paymentTransaction.getState()
                .equals(PaymentTransaction.PaymentState.COMPLETED))
            .flatMap(paymentTransaction -> getAppData(paymentTransaction.getBuyHash(),
                paymentTransaction.getPackageName(), paymentTransaction.getProductName())));
  }

  private Observable<AppCoinsOperation> getProofAppData() {
    return proofOfAttentionService.get()
        .subscribeOn(scheduler)
        .flatMap(proofs -> Observable.fromIterable(proofs)
            .filter(proof -> proof.getProofStatus()
                .equals(ProofStatus.COMPLETED))
            .flatMap(proof -> getAppData(proof.getHash(), proof.getPackageName(),
                proof.getCampaignId())));
  }

  private ObservableSource<? extends AppCoinsOperation> getAppData(String hash, String packageName,
      String data) {
    try {
      return Observable.just(appInfoProvider.get(hash, packageName, data));
    } catch (ImageSaver.SaveException | AppInfoProvider.UnknownApplicationException e) {
      return Observable.error(e);
    }
  }

  private Observable<Object> isKnownErrorError(Observable<Throwable> throwableObservable) {
    return throwableObservable.flatMap(throwable -> {
      if (throwable instanceof ImageSaver.SaveException
          || throwable instanceof AppInfoProvider.UnknownApplicationException) {
        return Observable.just(true);
      }
      return Observable.error(throwable);
    });
  }

  public Observable<AppCoinsOperation> get(String id) {
    return cache.get(id);
  }

  public void stop() {
    if (!disposables.isDisposed()) {
      disposables.dispose();
    }
    for (AppCoinsOperation inAppPurchaseData : cache.getAllSync()) {
      cache.removeSync(inAppPurchaseData.getTransactionId());
    }
  }

  public Observable<List<AppCoinsOperation>> getAll() {
    return cache.getAll();
  }
}

