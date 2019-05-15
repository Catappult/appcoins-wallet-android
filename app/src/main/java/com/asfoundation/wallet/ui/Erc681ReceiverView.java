package com.asfoundation.wallet.ui;

import com.asfoundation.wallet.entity.TransactionBuilder;

interface Erc681ReceiverView {
  String getCallingPackage();

  void startEipTransfer(TransactionBuilder transactionBuilder, Boolean isBds,
      String developerPayload);

  void startApp(Throwable throwable);

  void endAnimation();

  void showLoadingAnimation();
}
