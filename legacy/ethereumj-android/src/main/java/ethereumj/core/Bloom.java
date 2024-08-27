package ethereumj.core;

import ethereumj.util.ByteUtil;
import java.util.Arrays;
import org.spongycastle.util.encoders.Hex;

public class Bloom {

  final static int _8STEPS = 8;
  final static int _3LOW_BITS = 7;
  final static int ENSURE_BYTE = 255;

  byte[] data;

  public Bloom(byte[] data) {
    this.data = data;
  }

  public static Bloom create(byte[] toBloom) {

    int mov1 =
        (((toBloom[0] & ENSURE_BYTE) & (_3LOW_BITS)) << _8STEPS) + ((toBloom[1]) & ENSURE_BYTE);
    int mov2 =
        (((toBloom[2] & ENSURE_BYTE) & (_3LOW_BITS)) << _8STEPS) + ((toBloom[3]) & ENSURE_BYTE);
    int mov3 =
        (((toBloom[4] & ENSURE_BYTE) & (_3LOW_BITS)) << _8STEPS) + ((toBloom[5]) & ENSURE_BYTE);

    byte[] data = new byte[256];
    Bloom bloom = new Bloom(data);

    ByteUtil.setBit(data, mov1, 1);
    ByteUtil.setBit(data, mov2, 1);
    ByteUtil.setBit(data, mov3, 1);

    return bloom;
  }

  public void or(Bloom bloom) {
    for (int i = 0; i < data.length; ++i) {
      data[i] |= bloom.data[i];
    }
  }

  public byte[] getData() {
    return data;
  }

  public Bloom copy() {
    return new Bloom(Arrays.copyOf(getData(), getData().length));
  }

  @Override public int hashCode() {
    return data != null ? Arrays.hashCode(data) : 0;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Bloom bloom = (Bloom) o;

    return Arrays.equals(data, bloom.data);
  }

  @Override public String toString() {
    return Hex.toHexString(data);
  }
}
