package com.asfoundation.wallet.ui.iab;

public interface CreditCardNavigator {

  void popView(String transactionUid);

  void popViewWithError();
}
