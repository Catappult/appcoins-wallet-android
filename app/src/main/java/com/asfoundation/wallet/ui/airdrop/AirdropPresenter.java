package com.asfoundation.wallet.ui.airdrop;

import com.asfoundation.wallet.AirdropData;
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
    onCaptchaRefreshClick();
    onAirdropRequestClick();
    onAirdropStatusChange();
    onTerminateStateConsumed();
  }

  private void onTerminateStateConsumed() {
    disposables.add(view.getTerminateStateConsumed()
        .subscribe(__ -> airdrop.terminateStateConsumed()));
  }

  private void onCaptchaRefreshClick() {
    disposables.add(view.getCaptchaRefreshListener()
        .flatMapSingle(__ -> airdrop.requestCaptcha()
            .observeOn(scheduler)
            .doOnSuccess(view::showCaptcha)
            .doOnError(throwable -> throwable.printStackTrace()))
        .retry()
        .subscribe(__ -> {
        }, Throwable::printStackTrace));
  }

  private void onAirdropStatusChange() {
    disposables.add(airdrop.getStatus()
        .observeOn(scheduler)
        .subscribe(this::showAirdropStatus, Throwable::printStackTrace));
  }

  private void showAirdropStatus(AirdropData airdropData) {
    switch (airdropData.getStatus()) {
      case PENDING:
        view.showLoading();
        break;
      case ERROR:
        view.hideLoading();
        view.showGenericError();
        break;
      case API_ERROR:
        view.hideLoading();
        if (airdropData.getMessage() == null || airdropData.getMessage()
            .isEmpty()) {
          view.showGenericError();
        } else {
          view.showError(airdropData.getMessage());
        }
        break;
      case SUCCESS:
        view.hideLoading();
        view.showSuccess();
        break;
    }
  }

  private void onAirdropRequestClick() {
    disposables.add(view.getAirdropClick()
        .flatMapCompletable(airdrop::requestAirdrop)
        .subscribe(() -> {
        }, Throwable::printStackTrace));
  }

  private void showCaptcha() {
    disposables.add(airdrop.requestCaptcha()
        .observeOn(scheduler)
        .subscribe(view::showCaptcha, Throwable::printStackTrace));
  }

  public void stop() {
    if (!disposables.isDisposed()) {
      disposables.clear();
    }
  }
}
