package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;

public class PaymentMethodsPresenter {

  private final PaymentMethodsView view;
  private final Scheduler viewScheduler;
  private final Scheduler networkThread;
  private final CompositeDisposable disposables;
  private final InAppPurchaseInteractor inAppPurchaseInteractor;

  public PaymentMethodsPresenter(PaymentMethodsView view, Scheduler viewScheduler,
      Scheduler networkThread, CompositeDisposable disposables,
      InAppPurchaseInteractor inAppPurchaseInteractor) {
    this.view = view;
    this.viewScheduler = viewScheduler;
    this.networkThread = networkThread;
    this.disposables = disposables;
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
  }

  public void present(Bundle savedInstanceState) {
    if (savedInstanceState == null) {
      disposables.add(inAppPurchaseInteractor.getPaymentMethods()
          .subscribeOn(networkThread)
          .flatMap(paymentMethods -> Observable.fromIterable(paymentMethods)
              .map(paymentMethod -> new PaymentMethod(paymentMethod.getId(),
                  paymentMethod.getLabel(), paymentMethod.getIconUrl(), true))
              .toList())
          .observeOn(viewScheduler)
          .subscribe(view::showPaymentMethods, throwable -> {
            throwable.printStackTrace();
            view.showError();
          }));
    }
  }

  public void stop() {
    disposables.clear();
  }
}
