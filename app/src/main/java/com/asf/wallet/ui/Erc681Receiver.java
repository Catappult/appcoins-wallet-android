package com.asf.wallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.asf.wallet.interact.FindDefaultWalletInteract;
import com.asf.wallet.ui.iab.IabActivity;
import dagger.android.AndroidInjection;
import javax.inject.Inject;

/**
 * Created by trinkes on 13/03/2018.
 */

public class Erc681Receiver extends BaseActivity {

  public static final int REQUEST_CODE = 234;
  @Inject FindDefaultWalletInteract walletInteract;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    walletInteract.find()
        .subscribe(wallet -> startEipTransfer(), throwable -> startApp(throwable));
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
}
