package com.asfoundation.wallet.ui.iab;

import com.asfoundation.wallet.entity.TransactionBuilder;

/**
 * Created by franciscocalado on 20/07/2018.
 */

public interface IabView {

  void finish(String hash);

  void close();

  void setup(TransactionBuilder transactionBuilder, Boolean canBuy);
}
