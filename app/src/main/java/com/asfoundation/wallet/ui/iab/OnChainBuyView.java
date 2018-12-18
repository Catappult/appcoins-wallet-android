package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;
import com.jakewharton.rxrelay2.PublishRelay;
import io.reactivex.Observable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by franciscocalado on 19/07/2018.
 */

public interface OnChainBuyView {
  PublishRelay<String> getBuyClick();

  Observable<Object> getCancelClick();

  Observable<Object> getOkErrorClick();

  void showLoading();

  void close(Bundle data);

  void finish(Bundle data);

  void showError();

  void setup(String productName, boolean isDonation);

  void showTransactionCompleted();

  void showWrongNetworkError();

  void showNoNetworkError();

  void showApproving();

  void showBuying();

  void showNonceError();

  void showNoTokenFundsError();

  void showNoEtherFundsError();

  void showNoFundsError();

  void showRaidenChannelValues(List<BigDecimal> values);

  void showWallet(String wallet);
}
