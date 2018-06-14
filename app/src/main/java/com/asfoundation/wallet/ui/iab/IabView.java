package com.asfoundation.wallet.ui.iab;

import com.asfoundation.wallet.entity.TransactionBuilder;
import io.reactivex.Observable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by trinkes on 13/03/2018.
 */

public interface IabView {
  Observable<IabPresenter.BuyData> getBuyClick();

  Observable<Object> getCancelClick();

  Observable<Object> getOkErrorClick();

  void finish(String hash);

  void showLoading();

  void showError();

  void setup(TransactionBuilder transactionBuilder);

  void close();

  void showTransactionCompleted();

  void showBuy();

  void showWrongNetworkError();

  void showNoNetworkError();

  void showApproving();

  void showBuying();

  void showNonceError();

  void showNoTokenFundsError();

  void showNoEtherFundsError();

  void showNoFundsError();

  void showRaidenChannelValues(List<BigDecimal> values);

  Observable<Boolean> getCreateChannelClick();

  void showRaidenInfo();

  Observable<Object> getDontShowAgainClick();

  void showChannelAmount();

  void hideChannelAmount();

  void showChannelAsDefaultPayment();

  void showDefaultAsDefaultPayment();

  void showWallet(String wallet);

  void showNoChannelFundsError();
}
