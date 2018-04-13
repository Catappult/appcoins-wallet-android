package com.asfoundation.wallet.repository;

/**
 * Created by trinkes on 21/03/2018.
 */

public class TransactionNotFoundException extends RuntimeException {
  public TransactionNotFoundException() {
    super("Transaction not found");
  }
}
