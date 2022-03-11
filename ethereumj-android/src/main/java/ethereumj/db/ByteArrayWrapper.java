package ethereumj.db;

import ethereumj.util.FastByteComparisons;
import java.io.Serializable;
import java.util.Arrays;
import org.spongycastle.util.encoders.Hex;

public class ByteArrayWrapper implements Comparable<ByteArrayWrapper>, Serializable {

  private final byte[] data;
  private final int hashCode;

  public ByteArrayWrapper(byte[] data) {
    if (data == null) throw new NullPointerException("Data must not be null");
    this.data = data;
    this.hashCode = Arrays.hashCode(data);
  }

  @Override public int hashCode() {
    return hashCode;
  }

  public boolean equals(Object other) {
    if (!(other instanceof ByteArrayWrapper)) return false;
    byte[] otherData = ((ByteArrayWrapper) other).getData();
    return FastByteComparisons.compareTo(data, 0, data.length, otherData, 0, otherData.length) == 0;
  }

  @Override public String toString() {
    return Hex.toHexString(data);
  }

  @Override public int compareTo(ByteArrayWrapper o) {
    return FastByteComparisons.compareTo(data, 0, data.length, o.getData(), 0, o.getData().length);
  }

  public byte[] getData() {
    return data;
  }
}
