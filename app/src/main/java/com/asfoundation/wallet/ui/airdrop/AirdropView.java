package com.asfoundation.wallet.ui.airdrop;

import io.reactivex.Observable;

interface AirdropView {
  void showCaptcha(String captchaUrl);

  Observable<String> getAirdropClick();

  Observable<Object> getCaptchaRefreshListener();

  void showLoading();

  void hideLoading();

  void showGenericError();

  void showError(String message);

  void showSuccess();

  Observable<Object> getTerminateStateConsumed();

  void clearCaptchaText();

  void showCaptchaError();
}
