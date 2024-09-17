package ethereumj.config;

import java.math.BigInteger;

public class Constants {
  private static final BigInteger SECP256K1N =
      new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16);

  public static BigInteger getSECP256K1N() {
    return SECP256K1N;
  }
}
