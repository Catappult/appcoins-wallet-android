package com.asfoundation.wallet.ui.airdrop;

import com.asfoundation.wallet.service.AirdropInteractor;
import io.reactivex.disposables.CompositeDisposable;

public class AirdropPresenter {
  private final AirdropView view;
  private final CompositeDisposable disposables;

  public AirdropPresenter(AirdropView view, CompositeDisposable disposables,
      AirdropInteractor airdropInteractor) {
    this.view = view;
    this.disposables = disposables;
  }

  public void present() {
    showCaptcha();
  }

  public void showCaptcha() {

  }

  public void stop() {
    if (!disposables.isDisposed()) {
      disposables.dispose();
    }
  }
}
