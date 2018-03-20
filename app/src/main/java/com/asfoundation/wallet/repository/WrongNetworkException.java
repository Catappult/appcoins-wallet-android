package com.asfoundation.wallet.repository;

/**
 * Created by trinkes on 20/03/2018.
 */

public class WrongNetworkException extends Exception {
  public WrongNetworkException(String message) {
    super(message);
  }
}