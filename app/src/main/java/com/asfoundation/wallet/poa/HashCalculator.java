package com.asfoundation.wallet.poa;

import java.security.NoSuchAlgorithmException;

public class HashCalculator {
  private final String leadingString;
  private final Calculator calculator;

  public HashCalculator(int nonceLeadingZeros, Calculator calculator) {
    this.calculator = calculator;
    this.leadingString = String.format("%0" + nonceLeadingZeros + "d", 0);
  }

  public long calculateNonce(NonceData nonceData) throws NoSuchAlgorithmException {
    String data = nonceData.getPackageName() + nonceData.getTimeStamp();
    String hash = calculator.calculate(data.getBytes());

    String result;
    long nonce = -1;
    do {
      nonce++;
      data = nonce + hash;
      result = calculator.calculate(data.getBytes());
    } while (!result.substring(0, leadingString.length())
        .equals(leadingString));
    return nonce;
  }
}
