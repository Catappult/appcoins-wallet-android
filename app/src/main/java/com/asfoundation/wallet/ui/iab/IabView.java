package com.asfoundation.wallet.ui.iab;

import java.math.BigDecimal;

/**
 * Created by franciscocalado on 20/07/2018.
 */

public interface IabView {

  void finish(String hash);

  void close();

  void setup(BigDecimal amount, Boolean canBuy);

  void navigateToCreditCardAuthorization();
}
