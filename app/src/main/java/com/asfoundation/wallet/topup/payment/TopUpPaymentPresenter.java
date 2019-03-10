package com.asfoundation.wallet.topup.payment;

import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType;
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase;
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.appcoins.wallet.billing.repository.entity.TransactionData;
import com.asfoundation.wallet.billing.analytics.BillingAnalytics;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.repository.BdsPendingTransactionService;
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class TopUpPaymentPresenter {
  private final TopUpPaymentView view;
  private final String appPackage;
  private final InAppPurchaseInteractor inAppPurchaseInteractor;
  private final Scheduler viewScheduler;
  private final CompositeDisposable disposables;
  private final BillingMessagesMapper billingMessagesMapper;
  private final BdsPendingTransactionService bdsPendingTransactionService;
  private final Billing billing;
  private final boolean isBds;
  private final String uri;
  private final Single<TransactionBuilder> transactionBuilder;

  public TopUpPaymentPresenter(TopUpPaymentView view, String appPackage,
      InAppPurchaseInteractor inAppPurchaseInteractor, Scheduler viewScheduler,
      CompositeDisposable disposables, BillingMessagesMapper billingMessagesMapper,
      BdsPendingTransactionService bdsPendingTransactionService, Billing billing, boolean isBds,
      String uri) {
    this.view = view;
    this.appPackage = appPackage;
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
    this.viewScheduler = viewScheduler;
    this.disposables = disposables;
    this.billingMessagesMapper = billingMessagesMapper;
    this.bdsPendingTransactionService = bdsPendingTransactionService;
    this.billing = billing;
    this.isBds = isBds;
    this.uri = uri;
    this.transactionBuilder = inAppPurchaseInteractor.parseTransaction(uri, true);
  }

  public void present(String uri, double transactionValue, String currency) {
    setupUi(transactionValue, uri);
    handleErrorDismisses();
    handleOnGoingPurchases();
  }

  private void handleOnGoingPurchases() {
    disposables.add(transactionBuilder.flatMapCompletable(transactionBuilder -> {
      String skuId = transactionBuilder.getSkuId();
      if (skuId == null) {
        return Completable.complete();
      } else {
        return waitForUi(skuId);
      }
    })
        .observeOn(viewScheduler)
        .subscribe(view::hideLoading, throwable -> {
          view.showError();
          throwable.printStackTrace();
        }));
  }

  private Completable waitForUi(String skuId) {
    return Completable.mergeArray(checkProcessing(skuId), checkAndConsumePrevious(skuId),
        isSetupCompleted());
  }

  private Completable isSetupCompleted() {
    return view.setupUiCompleted()
        .takeWhile(isViewSet -> !isViewSet)
        .ignoreElements();
  }

  private Completable checkProcessing(String skuId) {
    return billing.getSkuTransaction(appPackage, skuId, Schedulers.io())
        .filter(transaction -> transaction.getStatus() == Transaction.Status.PROCESSING)
        .observeOn(AndroidSchedulers.mainThread())
        .map(Transaction::getUid)
        .observeOn(Schedulers.io())
        .flatMapCompletable(
            uid -> bdsPendingTransactionService.checkTransactionStateFromTransactionId(uid)
                .ignoreElements()
                .andThen(finishProcess(skuId)));
  }

  private Completable finishProcess(String skuId) {
    return billing.getSkuPurchase(appPackage, skuId, Schedulers.io())
        .observeOn(viewScheduler)
        .doOnSuccess(view::finish)
        .ignoreElement();
  }

  private Completable checkAndConsumePrevious(String sku) {
    return billing.getPurchases(appPackage, BillingSupportedType.INAPP, Schedulers.io())
        .flatMapObservable(purchases -> {
          for (Purchase purchase : purchases) {
            if (purchase.getUid()
                .equals(sku)) {
              return Observable.just(purchase);
            }
          }
          return Observable.empty();
        })
        .doOnNext(view::finish)
        .ignoreElements();
  }

  private void setupUi(double transactionValue, String uri) {
    disposables.add(
        Single.zip(transactionBuilder, inAppPurchaseInteractor.convertToLocalFiat(transactionValue),
            (transactionBuilder, fiatValue) -> Completable.fromAction(() -> view.setup(fiatValue))
                .subscribeOn(AndroidSchedulers.mainThread()))
            .flatMapCompletable(completable -> completable)
            .subscribe(() -> {
            }, this::showError));
  }

  private void showError(Throwable t) {
    view.showError();
  }

  public void stop() {
    if (!disposables.isDisposed()) {
      disposables.clear();
    }
  }

  private void close() {
    view.close();
  }

  private void handleErrorDismisses() {
    //disposables.add(view.errorDismisses()
    //    .doOnNext(__ -> close())
    //    .subscribe(__ -> {
    //    }, this::showError));
  }

  public boolean isBds() {
    return isBds;
  }
}
