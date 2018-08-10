package com.asfoundation.wallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.ui.iab.IabActivity;
import dagger.android.AndroidInjection;
import io.reactivex.disposables.Disposable;
import javax.inject.Inject;

/**
 * Created by trinkes on 13/03/2018.
 */

public class Erc681Receiver extends BaseActivity {

  public static final int REQUEST_CODE = 234;
  @Inject FindDefaultWalletInteract walletInteract;
  private Disposable disposable;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
  }

  private void startApp(Throwable throwable) {
    throwable.printStackTrace();
    startActivity(SplashActivity.newIntent(this));
    finish();
  }

  private void startEipTransfer() {
    Intent intent;
    if (getIntent().getData()
        .toString()
        .contains("/buy?")) {
      intent = IabActivity.newIntent(this, getIntent());
    } else {
      intent = SendActivity.newIntent(this, getIntent());
    }
    startActivityForResult(intent, REQUEST_CODE);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CODE) {
      setResult(resultCode, data);
      finish();
    }
  }

  @Override protected void onPause() {
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
    }
    super.onPause();
  }

  @Override protected void onResume() {
    super.onResume();
    disposable = walletInteract.find()
        .subscribe(wallet -> startEipTransfer(), throwable -> startApp(throwable));
  }
}
