package com.asfoundation.wallet.ui.airdrop;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;

public class AirdropPresenter {
  private final AirdropView view;
  private final CompositeDisposable disposables;
  private final AirdropInteractor airdrop;
  private final Scheduler scheduler;

  public AirdropPresenter(AirdropView view, CompositeDisposable disposables,
      AirdropInteractor airdrop, Scheduler scheduler) {
    this.view = view;
    this.disposables = disposables;
    this.airdrop = airdrop;
    this.scheduler = scheduler;
  }

  public void present() {
    showCaptcha();
    onAirdropRequestClick();
  }

  private void onAirdropRequestClick() {
    disposables.add(view.getAirdropClick()
        .flatMapCompletable(captchaAnswer -> airdrop.requestAirdrop(captchaAnswer))
        .subscribe());
  }

  private void showCaptcha() {
    disposables.add(airdrop.requestCaptcha()
        .observeOn(scheduler)
        .subscribe(view::showCaptcha, throwable -> throwable.printStackTrace()));
  }

  public void stop() {
    if (!disposables.isDisposed()) {
      disposables.dispose();
    }
  }
}
