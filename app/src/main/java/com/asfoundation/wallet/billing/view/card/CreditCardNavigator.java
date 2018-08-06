package com.asfoundation.wallet.billing.view.card;

public interface CreditCardNavigator {

  void popView(String transactionUid);

  void popViewWithError();
}
