package com.asfoundation.wallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import com.airbnb.lottie.LottieAnimationView;
import com.asf.wallet.R;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.interact.PaymentReceiverInteract;
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
  @Inject PaymentReceiverInteract paymentReceiverInteract;
  private Erc681ReceiverPresenter presenter;
  private LottieAnimationView walletCreationAnimation;
  private View walletCreationCard;
  private View walletCreationText;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_iab_wallet_creation);
    walletCreationCard = findViewById(R.id.create_wallet_card);
    walletCreationAnimation = findViewById(R.id.create_wallet_animation);
    walletCreationText = findViewById(R.id.create_wallet_text);
    presenter =
        new Erc681ReceiverPresenter(this, transferParser, inAppPurchaseInteractor, walletInteract,
            getIntent().getDataString(), paymentReceiverInteract);
    presenter.present(savedInstanceState);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CODE) {
      setResult(resultCode, data);
      finish();
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    walletCreationCard = null;
    walletCreationAnimation = null;
    walletCreationText = null;
  }

  @Override public void startEipTransfer(TransactionBuilder transaction, Boolean isBds,
      String developerPayload) {
    Intent intent;
    if (getIntent().getData()
        .toString()
        .contains("/buy?")) {
      intent = IabActivity.newIntent(this, getIntent(), transaction, isBds, developerPayload);
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

  @Override public void endAnimation() {
    walletCreationAnimation.setVisibility(View.INVISIBLE);
    walletCreationCard.setVisibility(View.INVISIBLE);
    walletCreationText.setVisibility(View.INVISIBLE);
    walletCreationAnimation.removeAllAnimatorListeners();
    walletCreationAnimation.removeAllUpdateListeners();
    walletCreationAnimation.removeAllLottieOnCompositionLoadedListener();
  }

  @Override public void showLoadingAnimation() {
    walletCreationAnimation.setVisibility(View.VISIBLE);
    walletCreationCard.setVisibility(View.VISIBLE);
    walletCreationText.setVisibility(View.VISIBLE);
    walletCreationAnimation.playAnimation();
  }

  @Override protected void onPause() {
    presenter.pause();
    super.onPause();
  }
}
