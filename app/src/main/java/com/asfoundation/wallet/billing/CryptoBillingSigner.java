package com.asfoundation.wallet.billing;

public interface CryptoBillingSigner {

  String getAddress();

  String getSignature();
}
