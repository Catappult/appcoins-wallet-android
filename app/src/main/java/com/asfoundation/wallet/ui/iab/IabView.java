package com.asfoundation.wallet.ui.iab;

import com.asfoundation.wallet.entity.TransactionBuilder;
import io.reactivex.Observable;

/**
 * Created by trinkes on 13/03/2018.
 */

public interface IabView {
  Observable<String> getBuyClick();

  Observable<Object> getCancelClick();

  Observable<Object> getOkErrorClick();

  void finish(String hash);

  void showLoading();

  void showError();

  void setup(TransactionBuilder transactionBuilder);

  void close();

  void showTransactionCompleted();

  void showBuy();
}
