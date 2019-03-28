package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType;
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase;
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.appcoins.wallet.billing.repository.entity.TransactionData;
import com.asfoundation.wallet.billing.analytics.BillingAnalytics;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.repository.BdsPendingTransactionService;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by franciscocalado on 24/07/2018.
 */

public class ExpressCheckoutBuyPresenter {
  private final ExpressCheckoutBuyView view;
  private final String appPackage;
  private final InAppPurchaseInteractor inAppPurchaseInteractor;
  private final Scheduler viewScheduler;
  private final CompositeDisposable disposables;
  private final BillingMessagesMapper billingMessagesMapper;
  private final BdsPendingTransactionService bdsPendingTransactionService;
  private final Billing billing;
  private final BillingAnalytics analytics;
  private final boolean isBds;
  private final String uri;
  private final Single<TransactionBuilder> transactionBuilder;
  private Scheduler ioScheduler;

  public ExpressCheckoutBuyPresenter(ExpressCheckoutBuyView view, String appPackage,
      InAppPurchaseInteractor inAppPurchaseInteractor, Scheduler viewScheduler,
      CompositeDisposable disposables, BillingMessagesMapper billingMessagesMapper,
      BdsPendingTransactionService bdsPendingTransactionService, Billing billing,
      BillingAnalytics analytics, boolean isBds, String uri, Scheduler ioScheduler) {
    this.view = view;
    this.appPackage = appPackage;
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
    this.viewScheduler = viewScheduler;
    this.disposables = disposables;
    this.billingMessagesMapper = billingMessagesMapper;
    this.bdsPendingTransactionService = bdsPendingTransactionService;
    this.billing = billing;
    this.analytics = analytics;
    this.isBds = isBds;
    this.uri = uri;
    this.ioScheduler = ioScheduler;
    this.transactionBuilder = inAppPurchaseInteractor.parseTransaction(uri, true);
  }

  public void present(String uri, double transactionValue, String currency) {
    handleCancelClick();
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
    return billing.getSkuTransaction(appPackage, skuId, ioScheduler)
        .filter(transaction -> transaction.getStatus() == Transaction.Status.PROCESSING)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess(__ -> view.showProcessingLoadingDialog())
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
    disposables.add(Single.zip(transactionBuilder,
        inAppPurchaseInteractor.convertToLocalFiat(transactionValue)
            .subscribeOn(ioScheduler),
        (transactionBuilder, fiatValue) -> Completable.fromAction(() -> view.setup(fiatValue,
            TransactionData.TransactionType.DONATION.name()
                .equalsIgnoreCase(transactionBuilder.getType())))
            .subscribeOn(viewScheduler))
        .flatMapCompletable(completable -> completable)
        .subscribe(() -> {
        }, this::showError));
  }

  private void handleCancelClick() {
    disposables.add(view.getCancelClick()
        .subscribe(click -> close()));
  }

  private void showError(Throwable t) {
    t.printStackTrace();
    view.showError();
  }

  public void stop() {
    if (!disposables.isDisposed()) {
      disposables.clear();
    }
  }

  private void close() {
    view.close(billingMessagesMapper.mapCancellation());
  }

  private void handleErrorDismisses() {
    disposables.add(view.errorDismisses()
        .doOnNext(__ -> close())
        .subscribe(__ -> {
        }, this::showError));
  }

  public boolean isBds() {
    return isBds;
  }
}
