package com.asf.wallet.ui.iab;

import com.asf.wallet.entity.TransactionBuilder;
import io.reactivex.Observable;

/**
 * Created by trinkes on 13/03/2018.
 */

public interface IabView {
  Observable<String> getBuyClick();

  Observable<Object> getCancelClick();

  void finish(String hash);

  void showLoading();

  void showError();

  void lockOrientation();

  void unlockOrientation();

  void setup(TransactionBuilder transactionBuilder);

  void close();
}
