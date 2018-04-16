package com.asfoundation.wallet.poa;

import com.google.gson.Gson;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;

public class HashCalculator {
  private final String leadingString;
  private final Gson gson;
  private final Calculator calculator;

  public HashCalculator(Gson gson, int nonceLeadingZeros, Calculator calculator) {
    this.gson = gson;
    this.calculator = calculator;
    this.leadingString = String.format("%0" + nonceLeadingZeros + "d", 0);
  }

  public String calculate(Object object) throws NoSuchAlgorithmException {
    if (object == null) {
      throw new NullPointerException("proof object is null");
    }

    return calculator.calculate(convertToBytes(gson.toJson(object)));
  }

  private byte[] convertToBytes(String data) {
    return data.getBytes(Charset.forName("UTF-8"));
  }

  public long calculateNonce(NonceData nonceData) throws NoSuchAlgorithmException {
    String result;
    long nonce = -1;
    String hash = calculate(nonceData);
    do {
      nonce++;
      result = calculator.calculate(convertToBytes(nonce + hash));
    } while (!result.substring(0, leadingString.length())
        .equals(leadingString));
    return nonce;
  }
}
