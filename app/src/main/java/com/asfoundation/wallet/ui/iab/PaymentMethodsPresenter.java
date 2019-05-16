package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;
import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.bdsbilling.WalletService;
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType;
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase;
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.appcoins.wallet.billing.repository.entity.TransactionData;
import com.appcoins.wallet.gamification.repository.ForecastBonus;
import com.asfoundation.wallet.billing.analytics.BillingAnalytics;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.repository.BdsPendingTransactionService;
import com.asfoundation.wallet.ui.gamification.GamificationInteractor;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;

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
  private final String developerPayload;
  private final String uri;
  private final WalletService walletService;
  private final GamificationInteractor gamification;
  private final TransactionBuilder transaction;

  public PaymentMethodsPresenter(PaymentMethodsView view, String appPackage,
      Scheduler viewScheduler, Scheduler networkThread, CompositeDisposable disposables,
      InAppPurchaseInteractor inAppPurchaseInteractor, BillingMessagesMapper billingMessagesMapper,
      BdsPendingTransactionService bdsPendingTransactionService, Billing billing,
      BillingAnalytics analytics, boolean isBds, String developerPayload, String uri,
      WalletService walletService, GamificationInteractor gamification,
      TransactionBuilder transaction) {
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
    this.developerPayload = developerPayload;
    this.uri = uri;
    this.walletService = walletService;
    this.gamification = gamification;
    this.transaction = transaction;
  }

  public void present(double transactionValue, Bundle savedInstanceState) {
    handleCancelClick();
    handleErrorDismisses();
    setupUi(transactionValue);
    handleOnGoingPurchases();
    handleBuyClick();
    if (isBds) {
      handlePaymentSelection();
    }
  }

  private void handlePaymentSelection() {
    disposables.add(view.getPaymentSelection()
        .flatMapCompletable(selectedPaymentMethod -> {
          if (selectedPaymentMethod.equals(PaymentMethodsView.SelectedPaymentMethod.APPC_CREDITS)
              || selectedPaymentMethod.equals(
              PaymentMethodsView.SelectedPaymentMethod.SHARE_LINK)) {
            return Completable.fromAction(view::hideBonus)
                .subscribeOn(viewScheduler);
          } else {
            return loadBonusIntoView().ignoreElement();
          }
        })
        .subscribe());
  }

  private Single<ForecastBonus> loadBonusIntoView() {
    return gamification.getEarningBonus(transaction.getDomain(), transaction.amount())
        .subscribeOn(networkThread)
        .observeOn(viewScheduler)
        .doOnSuccess(bonus -> {
          if (!bonus.getStatus()
              .equals(ForecastBonus.Status.ACTIVE)
              || bonus.getAmount()
              .compareTo(BigDecimal.ZERO) <= 0) {
            view.hideBonus();
          } else {
            view.showBonus(bonus.getAmount(), bonus.getCurrency());
          }
        });
  }

  private void handleBuyClick() {
    disposables.add(view.getBuyClick()
        .observeOn(viewScheduler)
        .doOnNext(selectedPaymentMethod -> {
          switch (selectedPaymentMethod) {
            case PAYPAL:
              view.showPaypal();
              break;
            case CREDIT_CARD:
              view.showCreditCard();
              break;
            case APPC:
              view.showAppCoins();
              break;
            case APPC_CREDITS:
              view.showCredits();
              break;
            case SHARE_LINK:
              view.showShareLink();
              break;
          }
        })
        .subscribe());
  }

  private void handleOnGoingPurchases() {
    if (transaction.getSkuId() == null) {
      return;
    }
    disposables.add(waitForUi(transaction.getSkuId()).observeOn(viewScheduler)
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
        .doOnSuccess(transaction -> handleProcessing())
        .map(Transaction::getUid)
        .observeOn(Schedulers.io())
        .flatMapCompletable(
            uid -> bdsPendingTransactionService.checkTransactionStateFromTransactionId(uid)
                .ignoreElements()
                .andThen(finishProcess(skuId)));
  }

  private void handleProcessing() {
    disposables.add(inAppPurchaseInteractor.getCurrentPaymentStep(appPackage, transaction)
        .filter(currentPaymentStep -> currentPaymentStep.equals(
            AsfInAppPurchaseInteractor.CurrentPaymentStep.PAUSED_ON_CHAIN))
        .doOnSuccess(currentPaymentStep -> inAppPurchaseInteractor.resume(uri,
            AsfInAppPurchaseInteractor.TransactionType.NORMAL, appPackage, transaction.getSkuId(),
            developerPayload, isBds))
        .subscribe());
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

  private void setupUi(double transactionValue) {
    setWalletAddress();
    disposables.add(Single.zip(isBds ? inAppPurchaseInteractor.getPaymentMethods()
            .subscribeOn(networkThread)
            .flatMap(paymentMethods -> Observable.fromIterable(paymentMethods)
                .map(paymentMethod -> new PaymentMethod(paymentMethod.getId(), paymentMethod.getLabel(),
                    paymentMethod.getIconUrl(), true))
                .toList()) : Single.just(Collections.singletonList(PaymentMethod.APPC)),
        isBds ? inAppPurchaseInteractor.getAvailablePaymentMethods(transaction)
            .subscribeOn(networkThread)
            .flatMap(paymentMethods -> Observable.fromIterable(paymentMethods)
                .map(paymentMethod -> new PaymentMethod(paymentMethod.getId(),
                    paymentMethod.getLabel(), paymentMethod.getIconUrl(), true))
                .toList()) : Single.just(Collections.singletonList(PaymentMethod.APPC))
            .observeOn(viewScheduler), inAppPurchaseInteractor.convertToLocalFiat(transactionValue)
            .subscribeOn(networkThread),
        (paymentMethods, availablePaymentMethods, fiatValue) -> Completable.fromAction(
            () -> view.showPaymentMethods(paymentMethods, availablePaymentMethods, fiatValue,
                TransactionData.TransactionType.DONATION.name()
                    .equalsIgnoreCase(transaction.getType()),
                mapCurrencyCodeToSymbol(fiatValue.getCurrency())))
            .subscribeOn(viewScheduler))
        .flatMapCompletable(completable -> completable)
        .subscribe(() -> {
        }, this::showError));
  }

  public String mapCurrencyCodeToSymbol(String currencyCode) {
    return currencyCode.equalsIgnoreCase("APPC") ? currencyCode : Currency.getInstance(currencyCode)
        .getCurrencyCode();
  }

  private void setWalletAddress() {
    disposables.add(walletService.getWalletAddress()
        .doOnSuccess(view::setWalletAddress)
        .subscribe(__ -> {
        }, Throwable::printStackTrace));
  }

  private void handleCancelClick() {
    disposables.add(view.getCancelClick()
        .subscribe(click -> close()));
  }

  private void showError(Throwable t) {
    t.printStackTrace();
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

  public void sendPurchaseDetailsEvent() {
    analytics.sendPurchaseDetailsEvent(appPackage, transaction.getSkuId(), transaction.amount()
        .toString(), transaction.getType());
  }

  public void stop() {
    disposables.clear();
  }
}
