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
  private final String appPackage;
  private final boolean isBds;
  private final TransactionBuilder transaction;

  public IabPresenter(IabView view, InAppPurchaseInteractor inAppPurchaseInteractor,
      Scheduler viewScheduler, CompositeDisposable disposables, String appPackage, boolean isBds,
      TransactionBuilder transaction) {
    this.view = view;
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
    this.viewScheduler = viewScheduler;
    this.disposables = disposables;
    this.appPackage = appPackage;
    this.isBds = isBds;
    this.transaction = transaction;
  }

  public void present(Bundle savedInstanceState) {
    if (savedInstanceState == null) {
      setupUi();
    }
  }

  private void setupUi() {
    if (isBds) {
      disposables.add(showBdsPayment(transaction).subscribe(() -> {
      }, this::showError));
    } else {
      disposables.add(Completable.fromAction(() -> view.showOnChain(transaction.amount(), false))
          .subscribe(() -> {
          }, this::showError));
    }
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