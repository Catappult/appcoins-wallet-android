package com.asfoundation.wallet.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.ui.iab.IabActivity;
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor;
import com.asfoundation.wallet.util.TransferParser;
import dagger.android.AndroidInjection;
import io.reactivex.disposables.Disposable;
import javax.inject.Inject;

import static com.asfoundation.wallet.ui.iab.IabActivity.PRODUCT_NAME;

public class OneStepPaymentReceiver extends BaseActivity {

  public static final int REQUEST_CODE = 234;
  @Inject InAppPurchaseInteractor inAppPurchaseInteractor;
  @Inject FindDefaultWalletInteract walletInteract;
  @Inject TransferParser transferParser;
  private Disposable disposable;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    if (savedInstanceState == null) {
      disposable = walletInteract.find()
          .flatMap(__ -> transferParser.parse(getIntent().getDataString())
              .flatMap(
                  transaction -> inAppPurchaseInteractor.isWalletFromBds(transaction.getDomain(),
                      transaction.toAddress())
                      .doOnSuccess(isBds -> startOneStepTransfer(transaction, isBds))))
          .subscribe(__ -> {
          }, throwable -> startApp(throwable));
    }
  }

  private void startApp(Throwable throwable) {
    throwable.printStackTrace();
    startActivity(SplashActivity.newIntent(this));
    finish();
  }

  private void startOneStepTransfer(TransactionBuilder transaction, boolean isBds) {
    Intent intent = IabActivity.newIntent(this, getIntent(), transaction, isBds, null);
    intent.putExtra(PRODUCT_NAME, transaction.getSkuId());
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
