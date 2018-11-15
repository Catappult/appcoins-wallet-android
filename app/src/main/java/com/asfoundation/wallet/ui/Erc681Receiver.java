package com.asfoundation.wallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.ui.iab.IabActivity;
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor;
import com.asfoundation.wallet.util.TransferParser;
import dagger.android.AndroidInjection;
import javax.inject.Inject;

/**
 * Created by trinkes on 13/03/2018.
 */

public class Erc681Receiver extends BaseActivity implements Erc681ReceiverView {

  public static final int REQUEST_CODE = 234;
  @Inject FindDefaultWalletInteract walletInteract;
  @Inject TransferParser transferParser;
  @Inject InAppPurchaseInteractor inAppPurchaseInteractor;
  private Erc681ReceiverPresenter presenter;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    presenter =
        new Erc681ReceiverPresenter(this, transferParser, inAppPurchaseInteractor, walletInteract,
            getIntent().getDataString());
    presenter.present(savedInstanceState);
  }

  @Override public void startEipTransfer(TransactionBuilder transaction, Boolean isBds) {
    Intent intent;
    if (getIntent().getData()
        .toString()
        .contains("/buy?")) {
      intent = IabActivity.newIntent(this, getIntent(), transaction);
    } else {
      intent = SendActivity.newIntent(this, getIntent());
    }
    startActivityForResult(intent, REQUEST_CODE);
  }

  @Override public void startApp(Throwable throwable) {
    throwable.printStackTrace();
    startActivity(SplashActivity.newIntent(this));
    finish();
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CODE) {
      setResult(resultCode, data);
      finish();
    }
  }

  @Override protected void onPause() {
    presenter.pause();
    super.onPause();
  }
}
