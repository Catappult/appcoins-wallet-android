package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;
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

public class PaymentMethodsPresenter {
  private final PaymentMethodsView view;
  private final Scheduler viewScheduler;
  private final Scheduler networkThread;
  private final CompositeDisposable disposables;
  private final InAppPurchaseInteractor inAppPurchaseInteractor;

  private final String appPackage;
  private final BillingMessagesMapper billingMessagesMapper;
  private final BdsPendingTransactionService bdsPendingTransactionService;
  private final Billing billing;
  private final BillingAnalytics analytics;
  private final boolean isBds;
  private final Single<TransactionBuilder> transactionBuilder;

  public PaymentMethodsPresenter(PaymentMethodsView view, String appPackage,
      Scheduler viewScheduler, Scheduler networkThread, CompositeDisposable disposables,
      InAppPurchaseInteractor inAppPurchaseInteractor, BillingMessagesMapper billingMessagesMapper,
      BdsPendingTransactionService bdsPendingTransactionService, Billing billing,
      BillingAnalytics analytics, boolean isBds, Single<TransactionBuilder> transactionBuilder) {
    this.view = view;
    this.appPackage = appPackage;
    this.viewScheduler = viewScheduler;
    this.networkThread = networkThread;
    this.disposables = disposables;
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
    this.billingMessagesMapper = billingMessagesMapper;
    this.bdsPendingTransactionService = bdsPendingTransactionService;
    this.billing = billing;
    this.analytics = analytics;
    this.isBds = isBds;
    this.transactionBuilder = transactionBuilder;
  }

  public void present(double transactionValue, String currency, Bundle savedInstanceState) {
    if (savedInstanceState == null) {
      handleCancelClick();
      setupUi(transactionValue, currency);
      handleErrorDismisses();
      handleOnGoingPurchases();
    }
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

  private Completable isSetupCompleted() {
    return view.setupUiCompleted()
        .takeWhile(isViewSet -> !isViewSet)
        .ignoreElements();
  }

  private Completable waitForUi(String skuId) {
    return Completable.mergeArray(checkProcessing(skuId), checkAndConsumePrevious(skuId),
        isSetupCompleted());
  }

  private Completable checkProcessing(String skuId) {
    return billing.getSkuTransaction(appPackage, skuId, Schedulers.io())
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

  private void setupUi(double transactionValue, String currency) {
    disposables.add(Single.zip(inAppPurchaseInteractor.getPaymentMethods()
            .subscribeOn(networkThread)
            .flatMap(paymentMethods -> Observable.fromIterable(paymentMethods)
                .map(paymentMethod -> new PaymentMethod(paymentMethod.getId(), paymentMethod.getLabel(),
                    paymentMethod.getIconUrl(), true))
                .toList())
            .observeOn(viewScheduler),
        inAppPurchaseInteractor.convertToFiat(transactionValue, currency),
        (paymentMethods, fiatValue) -> Completable.fromAction(
            () -> view.showPaymentMethods(paymentMethods, fiatValue,
                TransactionData.TransactionType.DONATION.name()
                    .equalsIgnoreCase(transactionBuilder.blockingGet()
                        .getType())))
            .subscribeOn(AndroidSchedulers.mainThread()))
        .flatMapCompletable(completable -> completable)
        .subscribe(() -> {
        }, this::showError));
  }

  private void handleCancelClick() {
    disposables.add(view.getCancelClick()
        .subscribe(click -> close()));
  }

  private void showError(Throwable t) {
    view.showError();
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

  public void sendPurchaseDetails(String purchaseDetails) {
    disposables.add(transactionBuilder.subscribe(
        transactionBuilder -> analytics.sendPurchaseDetailsEvent(appPackage,
            transactionBuilder.getSkuId(), transactionBuilder.amount()
                .toString(), purchaseDetails, transactionBuilder.getType())));
  }

  public void stop() {
    disposables.clear();
  }
}
