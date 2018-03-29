package com.asfoundation.wallet.poa;

import com.google.gson.Gson;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;

public class HashCalculator {
  private final Gson gson;
  private MessageDigest messageDigest;

  public HashCalculator(Gson gson, MessageDigest messageDigest) {
    this.gson = gson;
    this.messageDigest = messageDigest;
  }

  public String calculate(Object object) {
    if (object == null) {
      throw new NullPointerException("proof object is null");
    }

    messageDigest.update(gson.toJson(object)
        .getBytes(Charset.forName("UTF-8")));

    return String.format("%064x", new BigInteger(1, messageDigest.digest()));
  }
}
