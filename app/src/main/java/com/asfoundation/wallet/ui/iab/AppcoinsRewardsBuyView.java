package com.asfoundation.wallet.ui.iab;

import androidx.annotation.Nullable;
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase;
import io.reactivex.Observable;

interface AppcoinsRewardsBuyView {

  void finish(Purchase purchase);

  void showLoading();

  void hideLoading();

  void showNoNetworkError();

  Observable<Object> getOkErrorClick();

  void close();

  void showGenericError();

  void finish(String uid);

  void errorClose();

  void finish(Purchase purchase, @Nullable String orderReference);

  void showTransactionCompleted();

  long getAnimationDuration();
}
