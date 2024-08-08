package ethereumj.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.spongycastle.util.encoders.Hex;

public class ByteUtil {

  public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
  public static final byte[] ZERO_BYTE_ARRAY = { 0 };

  public static byte[] appendByte(byte[] bytes, byte b) {
    byte[] result = Arrays.copyOf(bytes, bytes.length + 1);
    result[result.length - 1] = b;
    return result;
  }

  public static byte[] bigIntegerToBytes(BigInteger b, int numBytes) {
    if (b == null) return null;
    byte[] bytes = new byte[numBytes];
    byte[] biBytes = b.toByteArray();
    int start = (biBytes.length == numBytes + 1) ? 1 : 0;
    int length = Math.min(biBytes.length, numBytes);
    System.arraycopy(biBytes, start, bytes, numBytes - length, length);
    return bytes;
  }

  public static byte[] bigIntegerToBytesSigned(BigInteger b, int numBytes) {
    if (b == null) return null;
    byte[] bytes = new byte[numBytes];
    Arrays.fill(bytes, b.signum() < 0 ? (byte) 0xFF : 0x00);
    byte[] biBytes = b.toByteArray();
    int start = (biBytes.length == numBytes + 1) ? 1 : 0;
    int length = Math.min(biBytes.length, numBytes);
    System.arraycopy(biBytes, start, bytes, numBytes - length, length);
    return bytes;
  }

  public static byte[] bigIntegerToBytes(BigInteger value) {
    if (value == null) return null;

    byte[] data = value.toByteArray();

    if (data.length != 1 && data[0] == 0) {
      byte[] tmp = new byte[data.length - 1];
      System.arraycopy(data, 1, tmp, 0, tmp.length);
      data = tmp;
    }
    return data;
  }

  public static BigInteger bytesToBigInteger(byte[] bb) {
    return bb.length == 0 ? BigInteger.ZERO : new BigInteger(1, bb);
  }

  public static byte[] longToBytesNoLeadZeroes(long val) {

    if (val == 0) return EMPTY_BYTE_ARRAY;

    byte[] data = ByteBuffer.allocate(8)
        .putLong(val)
        .array();

    return stripLeadingZeroes(data);
  }

  public static byte[] intToBytesNoLeadZeroes(int val) {

    if (val == 0) return EMPTY_BYTE_ARRAY;

    int lenght = 0;

    int tmpVal = val;
    while (tmpVal != 0) {
      tmpVal = tmpVal >>> 8;
      ++lenght;
    }

    byte[] result = new byte[lenght];

    int index = result.length - 1;
    while (val != 0) {

      result[index] = (byte) (val & 0xFF);
      val = val >>> 8;
      index -= 1;
    }

    return result;
  }

  public static String toHexString(byte[] data) {
    return data == null ? "" : Hex.toHexString(data);
  }

  public static int byteArrayToInt(byte[] b) {
    if (b == null || b.length == 0) return 0;
    return new BigInteger(1, b).intValue();
  }

  public static String nibblesToPrettyString(byte[] nibbles) {
    StringBuilder builder = new StringBuilder();
    for (byte nibble : nibbles) {
      String nibbleString = oneByteToHexString(nibble);
      builder.append("\\x")
          .append(nibbleString);
    }
    return builder.toString();
  }

  public static String oneByteToHexString(byte value) {
    String retVal = Integer.toString(value & 0xFF, 16);
    if (retVal.length() == 1) retVal = "0" + retVal;
    return retVal;
  }

  public static int firstNonZeroByte(byte[] data) {
    for (int i = 0; i < data.length; ++i) {
      if (data[i] != 0) {
        return i;
      }
    }
    return -1;
  }

  public static byte[] stripLeadingZeroes(byte[] data) {

    if (data == null) return null;

    int firstNonZero = firstNonZeroByte(data);
    switch (firstNonZero) {
      case -1:
        return ZERO_BYTE_ARRAY;

      case 0:
        return data;

      default:
        byte[] result = new byte[data.length - firstNonZero];
        System.arraycopy(data, firstNonZero, result, 0, data.length - firstNonZero);

        return result;
    }
  }

  public static byte[] copyToArray(BigInteger value) {
    byte[] src = bigIntegerToBytes(value);
    byte[] dest = ByteBuffer.allocate(32)
        .array();
    System.arraycopy(src, 0, dest, dest.length - src.length, src.length);
    return dest;
  }

  public static byte[] setBit(byte[] data, int pos, int val) {

    if ((data.length * 8) - 1 < pos) throw new Error("outside byte array limit, pos: " + pos);

    int posByte = data.length - 1 - (pos) / 8;
    int posBit = (pos) % 8;
    byte setter = (byte) (1 << (posBit));
    byte toBeSet = data[posByte];
    byte result;
    if (val == 1) {
      result = (byte) (toBeSet | setter);
    } else {
      result = (byte) (toBeSet & ~setter);
    }

    data[posByte] = result;
    return data;
  }

  public static byte[] and(byte[] b1, byte[] b2) {
    if (b1.length != b2.length) throw new RuntimeException("Array sizes differ");
    byte[] ret = new byte[b1.length];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = (byte) (b1[i] & b2[i]);
    }
    return ret;
  }

  public static byte[] or(byte[] b1, byte[] b2) {
    if (b1.length != b2.length) throw new RuntimeException("Array sizes differ");
    byte[] ret = new byte[b1.length];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = (byte) (b1[i] | b2[i]);
    }
    return ret;
  }

  public static byte[] merge(byte[]... arrays) {
    int arrCount = 0;
    int count = 0;
    for (byte[] array : arrays) {
      arrCount++;
      count += array.length;
    }

    byte[] mergedArray = new byte[count];
    int start = 0;
    for (byte[] array : arrays) {
      System.arraycopy(array, 0, mergedArray, start, array.length);
      start += array.length;
    }
    return mergedArray;
  }

  public static boolean isNullOrZeroArray(byte[] array) {
    return (array == null) || (array.length == 0);
  }

  public static boolean isSingleZero(byte[] array) {
    return (array.length == 1 && array[0] == 0);
  }

  public static int length(byte[]... bytes) {
    int result = 0;
    for (byte[] array : bytes) {
      result += (array == null) ? 0 : array.length;
    }
    return result;
  }
}