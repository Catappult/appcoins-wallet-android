package ethereumj.util;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import static ethereumj.util.ByteUtil.appendByte;
import static java.util.Arrays.copyOf;
import static java.util.Arrays.copyOfRange;
import static org.spongycastle.util.Arrays.concatenate;
import static org.spongycastle.util.encoders.Hex.encode;

public class CompactEncoder {

  private static final byte TERMINATOR = 16;
  private static final Map<Character, Byte> hexMap = new HashMap<>();

  static {
    hexMap.put('0', (byte) 0x0);
    hexMap.put('1', (byte) 0x1);
    hexMap.put('2', (byte) 0x2);
    hexMap.put('3', (byte) 0x3);
    hexMap.put('4', (byte) 0x4);
    hexMap.put('5', (byte) 0x5);
    hexMap.put('6', (byte) 0x6);
    hexMap.put('7', (byte) 0x7);
    hexMap.put('8', (byte) 0x8);
    hexMap.put('9', (byte) 0x9);
    hexMap.put('a', (byte) 0xa);
    hexMap.put('b', (byte) 0xb);
    hexMap.put('c', (byte) 0xc);
    hexMap.put('d', (byte) 0xd);
    hexMap.put('e', (byte) 0xe);
    hexMap.put('f', (byte) 0xf);
  }

  public static byte[] packNibbles(byte[] nibbles) {
    int terminator = 0;

    if (nibbles[nibbles.length - 1] == TERMINATOR) {
      terminator = 1;
      nibbles = copyOf(nibbles, nibbles.length - 1);
    }
    int oddlen = nibbles.length % 2;
    int flag = 2 * terminator + oddlen;
    if (oddlen != 0) {
      byte[] flags = { (byte) flag };
      nibbles = concatenate(flags, nibbles);
    } else {
      byte[] flags = { (byte) flag, 0 };
      nibbles = concatenate(flags, nibbles);
    }
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    for (int i = 0; i < nibbles.length; i += 2) {
      buffer.write(16 * nibbles[i] + nibbles[i + 1]);
    }
    return buffer.toByteArray();
  }

  public static boolean hasTerminator(byte[] packedKey) {
    return ((packedKey[0] >> 4) & 2) != 0;
  }

  public static byte[] unpackToNibbles(byte[] str) {
    byte[] base = binToNibbles(str);
    base = copyOf(base, base.length - 1);
    if (base[0] >= 2) {
      base = appendByte(base, TERMINATOR);
    }
    if (base[0] % 2 == 1) {
      base = copyOfRange(base, 1, base.length);
    } else {
      base = copyOfRange(base, 2, base.length);
    }
    return base;
  }

  public static byte[] binToNibbles(byte[] str) {

    byte[] hexEncoded = encode(str);
    byte[] hexEncodedTerminated = copyOf(hexEncoded, hexEncoded.length + 1);

    for (int i = 0; i < hexEncoded.length; ++i) {
      byte b = hexEncodedTerminated[i];
      hexEncodedTerminated[i] = hexMap.get((char) b);
    }

    hexEncodedTerminated[hexEncodedTerminated.length - 1] = TERMINATOR;
    return hexEncodedTerminated;
  }

  public static byte[] binToNibblesNoTerminator(byte[] str) {

    byte[] hexEncoded = encode(str);

    for (int i = 0; i < hexEncoded.length; ++i) {
      byte b = hexEncoded[i];
      hexEncoded[i] = hexMap.get((char) b);
    }

    return hexEncoded;
  }
}
