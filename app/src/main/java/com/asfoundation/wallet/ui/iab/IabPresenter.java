package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;
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

  public IabPresenter(IabView view, InAppPurchaseInteractor inAppPurchaseInteractor,
      Scheduler viewScheduler, CompositeDisposable disposables, String uriString,
      String appPackage) {
    this.view = view;
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
    this.viewScheduler = viewScheduler;
    this.disposables = disposables;
    this.uriString = uriString;
    this.appPackage = appPackage;
  }

  public void present(Bundle savedInstanceState) {
    if (savedInstanceState == null) {
      setupUi();
    }
  }

  private void setupUi() {
    disposables.add(inAppPurchaseInteractor.parseTransaction(uriString)
        .flatMap(transactionBuilder -> inAppPurchaseInteractor.getCurrentPaymentStep(appPackage,
            transactionBuilder)
            .observeOn(viewScheduler)
            .doOnSuccess(paymentStatus -> {
              switch (paymentStatus) {
                case PAUSED_ON_CHAIN:
                case READY:
                  view.showOnChain(transactionBuilder.amount());
                  break;
                case PAUSED_OFF_CHAIN:
                case NO_FUNDS:
                  view.showOffChain(transactionBuilder.amount());
                  break;
                default:
                  throw new NotImplementedError();
              }
            }))
        .subscribe(canBuy -> {
        }, this::showError));
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
