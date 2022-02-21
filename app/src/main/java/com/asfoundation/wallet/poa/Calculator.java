package com.asfoundation.wallet.poa;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.inject.Inject;

public class Calculator {

  public @Inject Calculator() {

  }

  public String calculate(byte[] bytes) throws NoSuchAlgorithmException {
    MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
    messageDigest.update(bytes);
    return String.format("%064x", new BigInteger(1, messageDigest.digest()));
  }
}
