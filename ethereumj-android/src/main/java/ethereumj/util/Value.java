package ethereumj.util;

import com.asf.util.DeepEquals;
import ethereumj.crypto.HashUtil;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import org.spongycastle.util.encoders.Hex;

public class Value {

  private Object value;
  private byte[] rlp;
  private byte[] sha3;

  private boolean decoded;

  public Value() {
  }

  public Value(Object obj) {

    this.decoded = true;
    if (obj == null) return;

    if (obj instanceof Value) {
      this.value = ((Value) obj).asObj();
    } else {
      this.value = obj;
    }
  }

  public static Value fromRlpEncoded(byte[] data) {

    if (data != null && data.length != 0) {
      Value v = new Value();
      v.init(data);
      return v;
    }
    return null;
  }

  public void init(byte[] rlp) {
    this.rlp = rlp;
  }

  public Value withHash(byte[] hash) {
    sha3 = hash;
    return this;
  }

  public Object asObj() {
    decode();
    return value;
  }

  public List<Object> asList() {
    decode();
    Object[] valueArray = (Object[]) value;
    return Arrays.asList(valueArray);
  }

  public int asInt() {
    decode();
    if (isInt()) {
      return (Integer) value;
    } else if (isBytes()) {
      return new BigInteger(1, asBytes()).intValue();
    }
    return 0;
  }

  public long asLong() {
    decode();
    if (isLong()) {
      return (Long) value;
    } else if (isBytes()) {
      return new BigInteger(1, asBytes()).longValue();
    }
    return 0;
  }

  public BigInteger asBigInt() {
    decode();
    return (BigInteger) value;
  }

  public String asString() {
    decode();
    if (isBytes()) {
      return new String((byte[]) value);
    } else if (isString()) {
      return (String) value;
    }
    return "";
  }

  public byte[] asBytes() {
    decode();
    if (isBytes()) {
      return (byte[]) value;
    } else if (isString()) {
      return asString().getBytes();
    }
    return ByteUtil.EMPTY_BYTE_ARRAY;
  }

  public String getHex() {
    return Hex.toHexString(this.encode());
  }

  public byte[] getData() {
    return this.encode();
  }

  public int[] asSlice() {
    return (int[]) value;
  }

  public Value get(int index) {
    if (isList()) {
      if (asList().size() <= index) {
        return new Value(null);
      }
      if (index < 0) {
        throw new RuntimeException("Negative index not allowed");
      }
      return new Value(asList().get(index));
    }
    return new Value(null);
  }

  public void decode() {
    if (!this.decoded) {
      this.value = RLP.decode(rlp, 0)
          .getDecoded();
      this.decoded = true;
    }
  }

  public byte[] encode() {
    if (rlp == null) rlp = RLP.encode(value);
    return rlp;
  }

  public byte[] hash() {
    if (sha3 == null) sha3 = HashUtil.sha3(encode());
    return sha3;
  }

  public boolean cmp(Value o) {
    return DeepEquals.deepEquals(this, o);
  }

  public boolean isList() {
    decode();
    return value != null && value.getClass()
        .isArray() && !value.getClass()
        .getComponentType()
        .isPrimitive();
  }

  public boolean isString() {
    decode();
    return value instanceof String;
  }

  public boolean isInt() {
    decode();
    return value instanceof Integer;
  }

  public boolean isLong() {
    decode();
    return value instanceof Long;
  }

  public boolean isBigInt() {
    decode();
    return value instanceof BigInteger;
  }

  public boolean isBytes() {
    decode();
    return value instanceof byte[];
  }

  public boolean isReadableString() {

    decode();
    int readableChars = 0;
    byte[] data = (byte[]) value;

    if (data.length == 1 && data[0] > 31 && data[0] < 126) {
      return true;
    }

    for (byte aData : data) {
      if (aData > 32 && aData < 126) ++readableChars;
    }

    return (double) readableChars / (double) data.length > 0.55;
  }

  public boolean isHexString() {

    decode();
    int hexChars = 0;
    byte[] data = (byte[]) value;

    for (byte aData : data) {

      if ((aData >= 48 && aData <= 57) || (aData >= 97 && aData <= 102)) ++hexChars;
    }

    return (double) hexChars / (double) data.length > 0.9;
  }

  public boolean isHashCode() {
    decode();
    return this.asBytes().length == 32;
  }

  public boolean isNull() {
    decode();
    return value == null;
  }

  public boolean isEmpty() {
    decode();
    if (isNull()) return true;
    if (isBytes() && asBytes().length == 0) return true;
    if (isList() && asList().isEmpty()) return true;
    return isString() && asString().equals("");
  }

  public int length() {
    decode();
    if (isList()) {
      return asList().size();
    } else if (isBytes()) {
      return asBytes().length;
    } else if (isString()) {
      return asString().length();
    }
    return 0;
  }

  public String toString() {

    decode();
    StringBuilder stringBuilder = new StringBuilder();

    if (isList()) {

      Object[] list = (Object[]) value;

      if (list.length == 2) {

        stringBuilder.append("[ ");

        Value key = new Value(list[0]);

        byte[] keyNibbles = CompactEncoder.binToNibblesNoTerminator(key.asBytes());
        String keyString = ByteUtil.nibblesToPrettyString(keyNibbles);
        stringBuilder.append(keyString);

        stringBuilder.append(",");

        Value val = new Value(list[1]);
        stringBuilder.append(val);

        stringBuilder.append(" ]");
        return stringBuilder.toString();
      }
      stringBuilder.append(" [");

      for (int i = 0; i < list.length; ++i) {
        Value val = new Value(list[i]);
        if (val.isString() || val.isEmpty()) {
          stringBuilder.append("'")
              .append(val)
              .append("'");
        } else {
          stringBuilder.append(val);
        }
        if (i < list.length - 1) stringBuilder.append(", ");
      }
      stringBuilder.append("] ");

      return stringBuilder.toString();
    } else if (isEmpty()) {
      return "";
    } else if (isBytes()) {

      StringBuilder output = new StringBuilder();
      if (isHashCode()) {
        output.append(Hex.toHexString(asBytes()));
      } else if (isReadableString()) {
        output.append("'");
        for (byte oneByte : asBytes()) {
          if (oneByte < 16) {
            output.append("\\x")
                .append(ByteUtil.oneByteToHexString(oneByte));
          } else {
            output.append(Character.valueOf((char) oneByte));
          }
        }
        output.append("'");
        return output.toString();
      }
      return Hex.toHexString(this.asBytes());
    } else if (isString()) {
      return asString();
    }
    return "Unexpected type";
  }

  public int countBranchNodes() {
    decode();
    if (this.isList()) {
      List<Object> objList = this.asList();
      int i = 0;
      for (Object obj : objList) {
        i += (new Value(obj)).countBranchNodes();
      }
      return i;
    } else if (this.isBytes()) {
      this.asBytes();
    }
    return 0;
  }
}
