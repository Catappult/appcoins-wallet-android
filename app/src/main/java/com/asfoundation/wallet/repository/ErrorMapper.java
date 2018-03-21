package com.asfoundation.wallet.repository;

import java.net.UnknownHostException;

/**
 * Created by trinkes on 20/03/2018.
 */

public class ErrorMapper {

  public static final String INSUFFICIENT_MESSAGE = "insufficient funds for gas * price + value";

  public PaymentTransaction.PaymentState map(Throwable throwable) {
    throwable.printStackTrace();
    if (throwable instanceof UnknownHostException) {
      return PaymentTransaction.PaymentState.NO_INTERNET;
    }
    if (throwable instanceof WrongNetworkException) {
      return PaymentTransaction.PaymentState.WRONG_NETWORK;
    }
    if (throwable.getMessage()
        .equalsIgnoreCase(INSUFFICIENT_MESSAGE)) {
      return PaymentTransaction.PaymentState.NO_FUNDS;
    }
    if (throwable instanceof TransactionNotFoundException) {
      return PaymentTransaction.PaymentState.ERROR;
    }
    return PaymentTransaction.PaymentState.ERROR;
  }
}
