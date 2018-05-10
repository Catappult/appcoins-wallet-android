package com.asfoundation.wallet.ui.airdrop;

import io.reactivex.Observable;

interface AirdropView {
  void showCaptcha(String captchaUrl);

  Observable<String> getAirdropClick();
}
