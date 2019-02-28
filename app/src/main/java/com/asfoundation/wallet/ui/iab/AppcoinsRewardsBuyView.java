package com.asfoundation.wallet.ui.iab;

import androidx.annotation.Nullable;
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase;
import io.reactivex.Observable;

interface AppcoinsRewardsBuyView {
  Observable<Object> getBuyClick();

  void showProcessingLoading();

  void setupView(String amount, String productName, String packageName, boolean isDonation);

  void showPaymentDetails();

  void hidePaymentDetails();

  void finish(Purchase purchase);

  void hideGenericLoading();

  void showLoading();

  void hideLoading();

  void showNoNetworkError();

  Observable<Object> getCancelClick();

  Observable<Object> getOkErrorClick();

  void close();

  void showGenericError();

  void finish(String uid);

  void errorClose();

  void finish(Purchase purchase, @Nullable String orderReference);
}
