package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;
import com.asfoundation.wallet.entity.TransactionBuilder;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by franciscocalado on 20/07/2018.
 */

public class IabPresenter {

  private static final String DEFAULT_CURRENCY = "EUR";

  private final IabView view;
  private final InAppPurchaseInteractor inAppPurchaseInteractor;
  private final Scheduler viewScheduler;
  private final CompositeDisposable disposables;
  private final boolean isBds;
  private final TransactionBuilder transaction;

  public IabPresenter(IabView view, InAppPurchaseInteractor inAppPurchaseInteractor,
      Scheduler viewScheduler, CompositeDisposable disposables, boolean isBds,
      TransactionBuilder transaction) {
    this.view = view;
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
    this.viewScheduler = viewScheduler;
    this.disposables = disposables;
    this.isBds = isBds;
    this.transaction = transaction;
  }

  public void present(Bundle savedInstanceState) {
    if (savedInstanceState == null) {
      view.showPaymentMethodsView();
    }
  }

  public void stop() {
    disposables.clear();
  }
}