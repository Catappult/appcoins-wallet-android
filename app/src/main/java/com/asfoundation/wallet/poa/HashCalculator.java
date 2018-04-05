package com.asfoundation.wallet.poa;

import com.google.gson.Gson;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;

public class HashCalculator {
  public final String leadingString;
  private final Gson gson;
  private final MessageDigest messageDigest;

  public HashCalculator(Gson gson, MessageDigest messageDigest, int nonceLeadingZeros) {
    this.gson = gson;
    this.messageDigest = messageDigest;
    this.leadingString = String.format("%0" + nonceLeadingZeros + "d", 0);
  }

  public String calculate(Object object) {
    if (object == null) {
      throw new NullPointerException("proof object is null");
    }

    return calculate(convertToBytes(gson.toJson(object)));
  }

  private byte[] convertToBytes(String data) {
    return data.getBytes(Charset.forName("UTF-8"));
  }

  public String calculate(byte[] bytes) {
    messageDigest.update(bytes);
    return String.format("%064x", new BigInteger(1, messageDigest.digest()));
  }

  public long calculateNonce(NonceData nonceData) {
    String result;
    long nonce = -1;
    String hash = calculate(nonceData);
    do {
      nonce++;
      result = calculate(convertToBytes(hash + nonce));
    } while (!result.substring(0, leadingString.length())
        .equals(leadingString));
    return nonce;
  }
}
