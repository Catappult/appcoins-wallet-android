package com.asfoundation.wallet.ui.airdrop;

import com.asfoundation.wallet.AirdropData;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;

public class AirdropPresenter {
  private final AirdropView view;
  private final CompositeDisposable disposables;
  private final AirdropInteractor airdrop;
  private final Scheduler scheduler;

  AirdropPresenter(AirdropView view, CompositeDisposable disposables, AirdropInteractor airdrop,
      Scheduler scheduler) {
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
    resetOnErrorState();
  }

  private void resetOnErrorState() {
    disposables.add(airdrop.getStatus()
        .filter(airdropData -> shouldClearCaptchaAnswer(airdropData.getStatus()))
        .observeOn(scheduler)
        .doOnNext(__ -> view.clearCaptchaText())
        .flatMapSingle(airdropData -> refreshCaptcha())
        .subscribe(__ -> {
        }, Throwable::printStackTrace));
  }

  private boolean shouldClearCaptchaAnswer(AirdropData.AirdropStatus status) {
    return status.equals(AirdropData.AirdropStatus.API_ERROR) || status.equals(
        AirdropData.AirdropStatus.ERROR) || status.equals(AirdropData.AirdropStatus.CAPTCHA_ERROR);
  }

  private void onTerminateStateConsumed() {
    disposables.add(view.getTerminateStateConsumed()
        .subscribe(__ -> airdrop.terminateStateConsumed()));
  }

  private void onCaptchaRefreshClick() {
    disposables.add(view.getCaptchaRefreshListener()
        .flatMapSingle(__ -> refreshCaptcha())
        .retry()
        .subscribe(__ -> {
        }, Throwable::printStackTrace));
  }

  private Single<String> refreshCaptcha() {
    return airdrop.requestCaptcha()
        .observeOn(scheduler)
        .doOnSuccess(view::showCaptcha)
        .doOnError(Throwable::printStackTrace);
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
      case CAPTCHA_ERROR:
        view.hideLoading();
        view.showCaptchaError();
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
