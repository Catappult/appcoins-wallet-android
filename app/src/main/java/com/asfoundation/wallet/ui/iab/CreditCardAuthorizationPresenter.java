package com.asfoundation.wallet.ui.iab;

import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by franciscocalado on 30/07/2018.
 */

public class CreditCardAuthorizationPresenter {

  private final Scheduler viewScheduler;
  private final CompositeDisposable disposables;
  private CreditCardAuthorizationView view;
  private FindDefaultWalletInteract defaultWalletInteract;

  public CreditCardAuthorizationPresenter(CreditCardAuthorizationView view,
      FindDefaultWalletInteract defaultWalletInteract, Scheduler viewScheduler,
      CompositeDisposable disposables) {
    this.view = view;
    this.defaultWalletInteract = defaultWalletInteract;
    this.viewScheduler = viewScheduler;
    this.disposables = disposables;
  }

  public void present() {
    disposables.add(defaultWalletInteract.find()
        .observeOn(viewScheduler)
        .doOnSuccess(wallet -> view.showWalletAddress(wallet.address))
        .subscribe(wallet -> {
        }, this::showError));
  }

  private void showError(Throwable t) {
  }

  public void stop() {
    disposables.clear();
  }
}
