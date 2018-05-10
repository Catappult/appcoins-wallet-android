package com.asfoundation.wallet.ui.airdrop;

import android.os.Bundle;
import android.support.annotation.Nullable;
import com.asfoundation.wallet.service.AirdropInteractor;
import dagger.android.support.DaggerFragment;
import io.reactivex.disposables.CompositeDisposable;
import javax.inject.Inject;

public class AirdropFragment extends DaggerFragment implements AirdropView {
  @Inject AirdropInteractor airdropInteractor;

  public static AirdropFragment newInstance() {
    return new AirdropFragment();
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    new AirdropPresenter(this, new CompositeDisposable(), airdropInteractor).present();
  }

  @Override public void showCaptcha(String captchaUrl) {

  }
}
