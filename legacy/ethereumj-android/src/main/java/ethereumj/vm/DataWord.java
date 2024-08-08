package ethereumj.vm;

import com.fasterxml.jackson.annotation.JsonValue;
import ethereumj.util.ByteUtil;
import ethereumj.util.FastByteComparisons;
import java.math.BigInteger;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.encoders.Hex;

public class DataWord implements Comparable<DataWord> {

  public static final DataWord ZERO = new DataWord(new byte[32]);

  public static final long MEM_SIZE = 32 + 16 + 16;

  private byte[] data = new byte[32];

  public DataWord(byte[] data) {
    if (data == null) {
      this.data = ByteUtil.EMPTY_BYTE_ARRAY;
    } else if (data.length == 32) {
      this.data = data;
    } else if (data.length <= 32) {
      System.arraycopy(data, 0, this.data, 32 - data.length, data.length);
    } else {
      throw new RuntimeException("Data word can't exceed 32 bytes: " + data);
    }
  }

  public byte[] getData() {
    return data;
  }

  public BigInteger value() {
    return new BigInteger(1, data);
  }

  public boolean isZero() {
    for (byte tmp : data) {
      if (tmp != 0) return false;
    }
    return true;
  }

  public boolean isNegative() {
    int result = data[0] & 0x80;
    return result == 0x80;
  }

  public DataWord and(DataWord w2) {

    for (int i = 0; i < this.data.length; ++i) {
      this.data[i] &= w2.data[i];
    }
    return this;
  }

  public DataWord or(DataWord w2) {

    for (int i = 0; i < this.data.length; ++i) {
      this.data[i] |= w2.data[i];
    }
    return this;
  }

  public void add(DataWord word) {
    byte[] result = new byte[32];
    for (int i = 31, overflow = 0; i >= 0; i--) {
      int v = (this.data[i] & 0xff) + (word.data[i] & 0xff) + overflow;
      result[i] = (byte) v;
      overflow = v >>> 8;
    }
    this.data = result;
  }

  @Override public int hashCode() {
    return java.util.Arrays.hashCode(data);
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DataWord dataWord = (DataWord) o;

    return java.util.Arrays.equals(data, dataWord.data);
  }

  public DataWord clone() {
    return new DataWord(Arrays.clone(data));
  }

  @JsonValue @Override public String toString() {
    return Hex.toHexString(data);
  }

  @Override public int compareTo(DataWord o) {
    if (o == null || o.getData() == null) return -1;
    int result =
        FastByteComparisons.compareTo(data, 0, data.length, o.getData(), 0, o.getData().length);
    return (int) Math.signum(result);
  }
}
