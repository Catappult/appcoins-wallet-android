package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.billing.BdsBilling;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.appcoins.wallet.billing.WalletService;
import com.appcoins.wallet.billing.mappers.ExternalBillingSerializer;
import com.appcoins.wallet.billing.repository.BdsRepository;
import com.appcoins.wallet.billing.repository.BillingSupportedType;
import com.appcoins.wallet.billing.repository.entity.Transaction;
import com.asfoundation.wallet.repository.BdsPendingTransactionService;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by franciscocalado on 24/07/2018.
 */

public class ExpressCheckoutBuyPresenter {
  private static final String TAG = ExpressCheckoutBuyPresenter.class.getSimpleName();
  private final ExpressCheckoutBuyView view;
  private final InAppPurchaseInteractor inAppPurchaseInteractor;
  private final Scheduler viewScheduler;
  private final WalletService walletService;
  private final CompositeDisposable disposables;
  private final BdsRepository bdsRepository;
  private final BillingMessagesMapper billingMessagesMapper;
  private final ExternalBillingSerializer billingSerializer;
  private final BdsPendingTransactionService bdsPendingTransactionService;
  private final BdsBilling bdsBilling;
  private PublishSubject<Boolean> consumePurchasesSubject;

  public ExpressCheckoutBuyPresenter(ExpressCheckoutBuyView view,
      InAppPurchaseInteractor inAppPurchaseInteractor, Scheduler viewScheduler,
      WalletService walletService, CompositeDisposable disposables, BdsRepository bdsRepository,
      BillingMessagesMapper billingMessagesMapper, ExternalBillingSerializer billingSerializer,
      BdsPendingTransactionService bdsPendingTransactionService, BdsBilling bdsBilling) {
    this.view = view;
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
    this.viewScheduler = viewScheduler;
    this.walletService = walletService;
    this.disposables = disposables;
    this.bdsRepository = bdsRepository;
    this.billingMessagesMapper = billingMessagesMapper;
    this.billingSerializer = billingSerializer;
    this.bdsPendingTransactionService = bdsPendingTransactionService;
    this.bdsBilling = bdsBilling;
    consumePurchasesSubject = PublishSubject.create();
  }

  public void present(double transactionValue, String currency, String appPackage, String skuId) {
    setupUi(transactionValue, currency);
    handleCancelClick();
    handleErrorDismisses();
    showDialog();
    handleOnGoingPurchases(appPackage, skuId);
  }

  private void handleOnGoingPurchases(String appPackage, String skuId) {
    disposables.add(checkProcessing(appPackage, skuId).onErrorComplete()
        .andThen(checkAndConsumePrevious())
        .subscribe(() -> {
        }, Throwable::printStackTrace));
  }

  private Completable checkProcessing(String appPackage, String skuId) {
    return walletService.getWalletAddress()
        .flatMap(walletAddress -> walletService.signContent(walletAddress)
            .flatMap(
                signedContent -> bdsRepository.getSkuTransaction(appPackage, skuId, walletAddress,
                    signedContent)))
        .filter(transaction -> transaction.getStatus() == Transaction.Status.PROCESSING)
        .observeOn(AndroidSchedulers.mainThread())
        .map(transaction1 -> {
          view.showProcessingLoadingDialog();
          return transaction1.getUid();
        })
        .observeOn(Schedulers.io())
        .flatMapObservable(
            uid -> bdsPendingTransactionService.checkTransactionStateFromTransactionId(uid)
                .flatMapMaybe(
                    __ -> bdsBilling.getPurchases(BillingSupportedType.INAPP, Schedulers.io())
                        .filter(purchases -> !purchases.isEmpty())
                        .map(purchases -> purchases.get(0)))
                .doOnNext(view::finish))
        .ignoreElements();
  }

  private Completable checkAndConsumePrevious() {
    return bdsBilling.getPurchases(BillingSupportedType.INAPP, Schedulers.io())
        .flatMapMaybe(purchases -> {
          if (purchases.isEmpty()) {
            consumePurchasesSubject.onNext(true);
            return null;
          } else {
            return Maybe.just(purchases);
          }
        })
        .map(purchases -> purchases.get(0))
        .flatMap(purchase -> walletService.getWalletAddress()
            .flatMap(walletAddress -> walletService.signContent(walletAddress)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(aBoolean -> {
                  view.finish(purchase);
                  consumePurchasesSubject.onNext(true);
                })
                .observeOn(Schedulers.io()))
            .toMaybe())
        .ignoreElement();
  }

  private void setupUi(double transactionValue, String currency) {
    disposables.add(inAppPurchaseInteractor.convertToFiat(transactionValue, currency)
        .observeOn(viewScheduler)
        .doOnSuccess(view::setup)
        .subscribe(__ -> {
        }, this::showError));
  }

  private void showDialog() {
    disposables.add(Observable.combineLatest(consumePurchasesSubject.take(1),
        view.setupUiCompleted()
            .take(1), (aBoolean, aBoolean2) -> aBoolean && aBoolean2)
        .filter(result -> result)
        .observeOn(viewScheduler)
        .doOnNext(__ -> view.hideLoading())
        .subscribe(__ -> {
        }, this::showError));
  }

  private void handleCancelClick() {
    disposables.add(view.getCancelClick()
        .subscribe(click -> close()));
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
    view.close(billingMessagesMapper.mapCancellation());
  }

  private void handleErrorDismisses() {
    disposables.add(view.errorDismisses()
        .doOnNext(__ -> close())
        .subscribe(__ -> {
        }, this::showError));
  }
}
