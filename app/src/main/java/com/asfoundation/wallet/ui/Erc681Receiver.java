package com.asfoundation.wallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.ui.iab.IabActivity;
import com.asfoundation.wallet.util.TransferParser;
import com.facebook.appevents.AppEventsLogger;
import dagger.android.AndroidInjection;
import io.reactivex.disposables.Disposable;
import javax.inject.Inject;

/**
 * Created by trinkes on 13/03/2018.
 */

public class Erc681Receiver extends BaseActivity {

  public static final int REQUEST_CODE = 234;
  @Inject FindDefaultWalletInteract walletInteract;
  @Inject TransferParser transferParser;
  private Disposable disposable;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    AppEventsLogger.newLogger(this)
        .logEvent("in_app_purchase_dialog_open");
    if (savedInstanceState == null) {
      disposable = walletInteract.find()
          .flatMap(__ -> transferParser.parse(getIntent().getDataString()))
          .subscribe(transaction -> {
            String callingPackage = getCallingPackage();
            if (callingPackage == null) {
              callingPackage = transaction.getDomain();
            }
            startEipTransfer(callingPackage);
          }, throwable -> startApp(throwable));
    }
  }

  private void startApp(Throwable throwable) {
    throwable.printStackTrace();
    startActivity(SplashActivity.newIntent(this));
    finish();
  }

  private void startEipTransfer(String callingPackage) {
    Intent intent;
    if (getIntent().getData()
        .toString()
        .contains("/buy?")) {
      intent = IabActivity.newIntent(this, getIntent(), callingPackage);
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
}
