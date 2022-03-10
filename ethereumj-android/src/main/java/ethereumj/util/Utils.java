package ethereumj.util;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.spongycastle.util.encoders.DecoderException;
import org.spongycastle.util.encoders.Hex;

public class Utils {

  private static final SecureRandom random = new SecureRandom();
  public static double JAVA_VERSION = getJavaVersion();
  static BigInteger _1000_ = new BigInteger("1000");

  public static BigInteger unifiedNumericToBigInteger(String number) {

    boolean match = Pattern.matches("0[xX][0-9a-fA-F]+", number);
    if (!match) {
      return (new BigInteger(number));
    } else {
      number = number.substring(2);
      number = number.length() % 2 != 0 ? "0".concat(number) : number;
      byte[] numberBytes = Hex.decode(number);
      return (new BigInteger(1, numberBytes));
    }
  }

  public static String longToDateTime(long timestamp) {
    Date date = new Date(timestamp * 1000);
    DateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    return formatter.format(date);
  }

  public static String getValueShortString(BigInteger number) {
    BigInteger result = number;
    int pow = 0;
    while (result.compareTo(_1000_) == 1 || result.compareTo(_1000_) == 0) {
      result = result.divide(_1000_);
      pow += 3;
    }
    return result + "\u00b7(" + "10^" + pow + ")";
  }

  public static byte[] addressStringToBytes(String hex) {
    byte[] addr;
    try {
      addr = Hex.decode(hex);
    } catch (DecoderException addressIsNotValid) {
      return null;
    }

    if (isValidAddress(addr)) return addr;
    return null;
  }

  public static boolean isValidAddress(byte[] addr) {
    return addr != null && addr.length == 20;
  }

  public static String getAddressShortString(byte[] addr) {

    if (!isValidAddress(addr)) throw new Error("not an address");

    String addrShort = Hex.toHexString(addr, 0, 3);

    StringBuffer sb = new StringBuffer();
    sb.append(addrShort);
    sb.append("...");

    return sb.toString();
  }

  public static SecureRandom getRandom() {
    return random;
  }

  static double getJavaVersion() {
    String version = System.getProperty("java.version");
    if (version.equals("0")) return 0;

    int pos = 0, count = 0;
    for (; pos < version.length() && count < 2; pos++) {
      if (version.charAt(pos) == '.') count++;
    }
    return Double.parseDouble(version.substring(0, pos - 1));
  }

  public static String getHashListShort(List<byte[]> blockHashes) {
    if (blockHashes.isEmpty()) return "[]";

    StringBuilder sb = new StringBuilder();
    String firstHash = Hex.toHexString(blockHashes.get(0));
    String lastHash = Hex.toHexString(blockHashes.get(blockHashes.size() - 1));
    return sb.append(" ")
        .append(firstHash)
        .append("...")
        .append(lastHash)
        .toString();
  }

  public static String getNodeIdShort(String nodeId) {
    return nodeId == null ? "<null>" : nodeId.substring(0, 8);
  }

  public static long toUnixTime(long javaTime) {
    return javaTime / 1000;
  }

  public static long fromUnixTime(long unixTime) {
    return unixTime * 1000;
  }

  public static <T> T[] mergeArrays(T[]... arr) {
    int size = 0;
    for (T[] ts : arr) {
      size += ts.length;
    }
    T[] ret = (T[]) Array.newInstance(arr[0].getClass()
        .getComponentType(), size);
    int off = 0;
    for (T[] ts : arr) {
      System.arraycopy(ts, 0, ret, off, ts.length);
      off += ts.length;
    }
    return ret;
  }

  public static String align(String s, char fillChar, int targetLen, boolean alignRight) {
    if (targetLen <= s.length()) return s;
    String alignString = repeat("" + fillChar, targetLen - s.length());
    return alignRight ? alignString + s : s + alignString;
  }

  public static String repeat(String s, int n) {
    if (s.length() == 1) {
      byte[] bb = new byte[n];
      Arrays.fill(bb, s.getBytes()[0]);
      return new String(bb);
    } else {
      StringBuilder ret = new StringBuilder();
      for (int i = 0; i < n; i++) ret.append(s);
      return ret.toString();
    }
  }
}