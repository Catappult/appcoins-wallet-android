package com.asfoundation.wallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import com.airbnb.lottie.LottieAnimationView;
import com.asf.wallet.R;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.interact.PaymentReceiverInteract;
import com.asfoundation.wallet.repository.WalletNotFoundException;
import com.asfoundation.wallet.ui.iab.IabActivity;
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor;
import com.asfoundation.wallet.util.TransferParser;
import dagger.android.AndroidInjection;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import javax.inject.Inject;

import static com.asfoundation.wallet.ui.iab.IabActivity.PRODUCT_NAME;

public class OneStepPaymentReceiver extends BaseActivity {

  public static final int REQUEST_CODE = 234;
  @Inject InAppPurchaseInteractor inAppPurchaseInteractor;
  @Inject FindDefaultWalletInteract walletInteract;
  @Inject PaymentReceiverInteract paymentReceiverInteract;
  @Inject TransferParser transferParser;
  private Disposable disposable;
  private View walletCreationCard;
  private LottieAnimationView walletCreationAnimation;
  private View walletCreationText;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_iab_wallet_creation);
    walletCreationCard = findViewById(R.id.create_wallet_card);
    walletCreationAnimation = findViewById(R.id.create_wallet_animation);
    walletCreationText = findViewById(R.id.create_wallet_text);
    if (savedInstanceState == null) {
      disposable = walletInteract.find()
          .onErrorResumeNext(throwable -> throwable instanceof WalletNotFoundException
              ? createWallet().doAfterTerminate(this::endAnimation) : Single.error(throwable))
          .flatMap(__ -> transferParser.parse(getIntent().getDataString())
              .flatMap(
                  transaction -> inAppPurchaseInteractor.isWalletFromBds(transaction.getDomain(),
                      transaction.toAddress())
                      .doOnSuccess(isBds -> startOneStepTransfer(transaction, isBds))))
          .subscribe(__ -> {
          }, this::startApp);
    }
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CODE) {
      setResult(resultCode, data);
      finish();
    }
  }

  private void startApp(Throwable throwable) {
    throwable.printStackTrace();
    startActivity(SplashActivity.newIntent(this));
    finish();
  }

  private void startOneStepTransfer(TransactionBuilder transaction, boolean isBds) {
    Intent intent =
        IabActivity.newIntent(this, getIntent(), transaction, isBds, transaction.getPayload());
    intent.putExtra(PRODUCT_NAME, transaction.getSkuId());
    startActivityForResult(intent, REQUEST_CODE);
  }

  @Override protected void onPause() {
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
    }
    super.onPause();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    walletCreationCard = null;
    walletCreationAnimation = null;
    walletCreationText = null;
  }

  private Single<Wallet> createWallet() {
    showLoadingAnimation();
    return paymentReceiverInteract.createWallet();
  }

  private void endAnimation() {
    walletCreationAnimation.setVisibility(View.INVISIBLE);
    walletCreationCard.setVisibility(View.INVISIBLE);
    walletCreationText.setVisibility(View.INVISIBLE);
    walletCreationAnimation.removeAllAnimatorListeners();
    walletCreationAnimation.removeAllUpdateListeners();
    walletCreationAnimation.removeAllLottieOnCompositionLoadedListener();
  }

  private void showLoadingAnimation() {
    walletCreationAnimation.setVisibility(View.VISIBLE);
    walletCreationCard.setVisibility(View.VISIBLE);
    walletCreationText.setVisibility(View.VISIBLE);
    walletCreationAnimation.playAnimation();
  }
}
