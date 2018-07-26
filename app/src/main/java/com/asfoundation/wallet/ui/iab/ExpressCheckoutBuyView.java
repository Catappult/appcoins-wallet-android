package com.asfoundation.wallet.ui.iab;

import com.asfoundation.wallet.entity.TransactionBuilder;
import io.reactivex.Observable;

/**
 * Created by franciscocalado on 20/07/2018.
 */

public interface ExpressCheckoutBuyView {

  void setup(TransactionBuilder transactionBuilder, FiatValue convertToFiatResponseBody);

  void showError();

  Observable<Object> getCancelClick();

  void close();
}
