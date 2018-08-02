package com.asfoundation.wallet.billing;

public interface BillingSigner {

  String getAddress();

  String getSignature();
}
