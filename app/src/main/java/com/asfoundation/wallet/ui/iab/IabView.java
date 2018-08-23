package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;
import java.math.BigDecimal;

/**
 * Created by franciscocalado on 20/07/2018.
 */

public interface IabView {

  void finish(Bundle data);

  void showError();

  void close(Bundle bundle);

  void setup(BigDecimal amount, Boolean canBuy);

  void navigateToCreditCardAuthorization();

  void show(InAppPurchaseInteractor.CurrentPaymentStep canBuy);

  void showOnChain(BigDecimal amount);
}
