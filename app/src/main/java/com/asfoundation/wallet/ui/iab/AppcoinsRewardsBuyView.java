package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.bdsbilling.repository.entity.Purchase;
import io.reactivex.Observable;

interface AppcoinsRewardsBuyView {
  Observable<Object> getBuyClick();

  void showLoading();

  void setAmount(String amount);

  void showPaymentDetails();

  void hidePaymentDetails();

  void finish(Purchase purchase);

  void hideLoading();
}
