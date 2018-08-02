package com.asfoundation.wallet.ui.iab;

import io.reactivex.Observable;

/**
 * Created by franciscocalado on 20/07/2018.
 */

public interface ExpressCheckoutBuyView {

  void setup(FiatValue convertToFiatResponseBody);

  void showError();

  Observable<Object> getCancelClick();

  void close();

  Observable<Object> errorDismisses();
}
