package ethereumj.util;

import java.math.BigInteger;

public class BIUtil {

  public static boolean isLessThan(BigInteger valueA, BigInteger valueB) {
    return valueA.compareTo(valueB) < 0;
  }
}
