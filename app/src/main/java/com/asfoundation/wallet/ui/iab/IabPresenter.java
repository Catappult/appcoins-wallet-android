package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;
import com.asfoundation.wallet.billing.analytics.BillingAnalytics;
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
  private final IabView view;
  private final InAppPurchaseInteractor inAppPurchaseInteractor;
  private final Scheduler viewScheduler;
  private final CompositeDisposable disposables;
  private final String uriString;
  private final String appPackage;
  private final boolean isBds;
  private final BillingAnalytics analytics;

  public IabPresenter(IabView view, InAppPurchaseInteractor inAppPurchaseInteractor,
      Scheduler viewScheduler, CompositeDisposable disposables, String uriString, String appPackage,
      boolean isBds, BillingAnalytics analytics) {
    this.view = view;
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
    this.viewScheduler = viewScheduler;
    this.disposables = disposables;
    this.uriString = uriString;
    this.appPackage = appPackage;
    this.isBds = isBds;
    this.analytics = analytics;
  }

  public void present(Bundle savedInstanceState) {
    if (savedInstanceState == null) {
      setupUi();
    }
  }

  private void setupUi() {
    disposables.add(inAppPurchaseInteractor.parseTransaction(uriString, isBds)
        .flatMapCompletable(transaction -> {
          if (isBds) {
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
    return Completable.fromAction(() -> view.showOnChain(transaction.amount()));
  }

  private Completable showBdsPayment(TransactionBuilder transactionBuilder) {
    return inAppPurchaseInteractor.getPaymentMethod(appPackage, transactionBuilder)
        .subscribeOn(viewScheduler)
        .doOnSuccess(gateway -> {
          switch (gateway) {
            case appcoins:
              view.showOnChain(transactionBuilder.amount());
              break;
            case adyen:
              view.showCcPayment(transactionBuilder.amount());
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


  public void sendCCDetailsEvent() {
    if (isBds) {
      analytics.sendCreditCardDetailsEvent(appPackage, inAppPurchaseInteractor.parseTransaction(uriString, isBds).blockingGet().getSkuId(),
              inAppPurchaseInteractor.parseTransaction(uriString, isBds).blockingGet().amount().toString());
    }
  }

  public void sendPurchaseDetails(String purchaseDetails) {
    if (isBds) {
      analytics.sendPurchaseDetailsEvent(appPackage, inAppPurchaseInteractor.parseTransaction(uriString, isBds).blockingGet().getSkuId(),
              inAppPurchaseInteractor.parseTransaction(uriString, isBds).blockingGet().amount().toString(), purchaseDetails);
    }
  }

  public void sendPaymentEvent(){
    if(isBds){
      analytics.sendPaymentEvent(appPackage, inAppPurchaseInteractor.parseTransaction(uriString, isBds).blockingGet().getSkuId(),
              inAppPurchaseInteractor.parseTransaction(uriString, isBds).blockingGet().amount().toString());
    }
  }
}