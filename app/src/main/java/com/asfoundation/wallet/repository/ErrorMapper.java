package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.util.UnknownTokenException;
import java.net.UnknownHostException;

/**
 * Created by trinkes on 20/03/2018.
 */

public class ErrorMapper {

  public static final String INSUFFICIENT_ERROR_MESSAGE =
      "insufficient funds for gas * price + value";
  public static final String NONCE_TOO_LOW_ERROR_MESSAGE = "nonce too low";

  public PaymentTransaction.PaymentState map(Throwable throwable) {
    throwable.printStackTrace();
    if (throwable instanceof UnknownHostException) {
      return PaymentTransaction.PaymentState.NO_INTERNET;
    }
    if (throwable instanceof WrongNetworkException) {
      return PaymentTransaction.PaymentState.WRONG_NETWORK;
    }
    if (throwable instanceof TransactionNotFoundException) {
      return PaymentTransaction.PaymentState.ERROR;
    }
    if (throwable instanceof UnknownTokenException) {
      return PaymentTransaction.PaymentState.UNKNOWN_TOKEN;
    }
    if (throwable instanceof TransactionException) {
      switch (throwable.getMessage()) {
        case INSUFFICIENT_ERROR_MESSAGE:
          return PaymentTransaction.PaymentState.NO_FUNDS;
        case NONCE_TOO_LOW_ERROR_MESSAGE:
          return PaymentTransaction.PaymentState.NONCE_ERROR;
      }
    }
    return PaymentTransaction.PaymentState.ERROR;
  }
}
