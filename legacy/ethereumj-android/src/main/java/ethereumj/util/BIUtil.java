package ethereumj.util;

import java.math.BigInteger;

public class BIUtil {

  public static boolean isZero(BigInteger value) {
    return value.compareTo(BigInteger.ZERO) == 0;
  }

  public static boolean isEqual(BigInteger valueA, BigInteger valueB) {
    return valueA.compareTo(valueB) == 0;
  }

  public static boolean isNotEqual(BigInteger valueA, BigInteger valueB) {
    return !isEqual(valueA, valueB);
  }

  public static boolean isLessThan(BigInteger valueA, BigInteger valueB) {
    return valueA.compareTo(valueB) < 0;
  }

  public static boolean isMoreThan(BigInteger valueA, BigInteger valueB) {
    return valueA.compareTo(valueB) > 0;
  }

  public static BigInteger sum(BigInteger valueA, BigInteger valueB) {
    return valueA.add(valueB);
  }

  public static BigInteger toBI(byte[] data) {
    return new BigInteger(1, data);
  }

  public static BigInteger toBI(long data) {
    return BigInteger.valueOf(data);
  }

  public static boolean isPositive(BigInteger value) {
    return value.signum() > 0;
  }

  public static boolean isCovers(BigInteger covers, BigInteger value) {
    return !isNotCovers(covers, value);
  }

  public static boolean isNotCovers(BigInteger covers, BigInteger value) {
    return covers.compareTo(value) < 0;
  }

  public static boolean exitLong(BigInteger value) {

    return (value.compareTo(new BigInteger(Long.MAX_VALUE + ""))) > -1;
  }

  public static boolean isIn20PercentRange(BigInteger first, BigInteger second) {
    BigInteger five = BigInteger.valueOf(5);
    BigInteger limit = first.add(first.divide(five));
    return !isMoreThan(second, limit);
  }

  public static BigInteger max(BigInteger first, BigInteger second) {
    return first.compareTo(second) < 0 ? second : first;
  }
}
