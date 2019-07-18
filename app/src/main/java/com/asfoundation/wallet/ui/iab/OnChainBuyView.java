package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;
import io.reactivex.Observable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by franciscocalado on 19/07/2018.
 */

public interface OnChainBuyView {

  Observable<Object> getOkErrorClick();

  void showLoading();

  void close(Bundle data);

  void finish(Bundle data);

  void showError();

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

  long getAnimationDuration();
}
