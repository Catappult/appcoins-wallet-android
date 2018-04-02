package com.asfoundation.wallet.repository;

/**
 * Created by trinkes on 21/03/2018.
 */

public class TransactionException extends RuntimeException {
  private final int code;
  private final String data;

  public TransactionException(int code, String message, String data) {
    super(message);
    this.code = code;
    this.data = data;
  }

  public int getCode() {
    return code;
  }

  public String getData() {
    return data;
  }
}
