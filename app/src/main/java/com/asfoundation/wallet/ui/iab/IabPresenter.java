package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;
import com.asfoundation.wallet.entity.TransactionBuilder;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import javax.annotation.Nullable;
import kotlin.NotImplementedError;

/**
 * Created by franciscocalado on 20/07/2018.
 */

public class IabPresenter {

  private static final String DEFAULT_CURRENCY = "EUR";

  private final IabView view;
  private final InAppPurchaseInteractor inAppPurchaseInteractor;
  private final Scheduler viewScheduler;
  private final CompositeDisposable disposables;
  private final String uriString;
  private final String appPackage;
  private final boolean isIap;

  public IabPresenter(IabView view, InAppPurchaseInteractor inAppPurchaseInteractor,
      Scheduler viewScheduler, CompositeDisposable disposables, String uriString, String appPackage,
      boolean isIap) {
    this.view = view;
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
    this.viewScheduler = viewScheduler;
    this.disposables = disposables;
    this.uriString = uriString;
    this.appPackage = appPackage;
    this.isIap = isIap;
  }

  public void present(Bundle savedInstanceState) {
    if (savedInstanceState == null) {
      setupUi();
    }
  }

  private void setupUi() {
    disposables.add(inAppPurchaseInteractor.parseTransaction(uriString, isIap)
        .flatMapCompletable(transaction -> {
          if (isIap) {
            return showBdsPayment(transaction);
          }
          return inAppPurchaseInteractor.isWalletFromBds(appPackage, transaction.toAddress())
              .flatMapCompletable(
                  isWalletFromBds -> showGenericPayment(transaction, isWalletFromBds));
        })
        .subscribe(() -> {
        }, this::showError));
  }

  private Completable showGenericPayment(TransactionBuilder transaction, Boolean isWalletFromBds) {
    if (isWalletFromBds) {
      return showBdsPayment(transaction);
    }
    return Completable.fromAction(() -> view.showOnChain(transaction.amount(), false));
  }

  private Completable showBdsPayment(TransactionBuilder transactionBuilder) {
    return inAppPurchaseInteractor.getPaymentMethod(appPackage, transactionBuilder)
        .subscribeOn(viewScheduler)
        .doOnSuccess(gateway -> {
          switch (gateway) {
            case appcoins:
              view.showOnChain(transactionBuilder.amount(), true);
              break;
            case adyen:
              view.showCcPayment(transactionBuilder.amount(), DEFAULT_CURRENCY, true);
              break;
            case appcoins_credits:
              view.showAppcoinsCreditsPayment(transactionBuilder.amount());
              break;
            case unknown:
            default:
              throw new NotImplementedError();
          }
        })
        .ignoreElement();
  }

  private void showError(@Nullable Throwable throwable) {
    if (throwable != null) {
      throwable.printStackTrace();
    }
  }

  public void stop() {
    disposables.clear();
  }
}