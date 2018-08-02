package com.asfoundation.wallet.ui.iab;

/**
 * Created by franciscocalado on 20/07/2018.
 */

public interface IabView {

  void finish(String hash);

  void close();

  void setup(double amount, Boolean canBuy);

  void navigateToCreditCardAuthorization();
}
