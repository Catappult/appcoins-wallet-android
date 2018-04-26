package com.asfoundation.wallet.poa;

import com.google.gson.Gson;
import java.nio.ByteBuffer;
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
    int bytesSize;
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
      bytesSize = Long.BYTES;
    } else {
      bytesSize = 8;
    }

    ByteBuffer buffer = ByteBuffer.allocate(nonceData.getPackageName()
        .length() + bytesSize);
    buffer.put(nonceData.getPackageName()
        .getBytes());
    buffer.putLong(nonceData.getTimeStamp());
    String hash = calculator.calculate(buffer.array());

    buffer = ByteBuffer.allocate(bytesSize + hash.length());
    String result;
    long nonce = -1;
    do {
      buffer.clear();
      nonce++;
      buffer.putLong(nonce);
      buffer.put(hash.getBytes());
      result = calculator.calculate(buffer.array());
    } while (!result.substring(0, leadingString.length())
        .equals(leadingString));
    return nonce;
  }
}
