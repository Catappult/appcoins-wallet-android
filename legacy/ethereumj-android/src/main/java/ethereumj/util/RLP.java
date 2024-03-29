package ethereumj.util;

import ethereumj.db.ByteArrayWrapper;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;
import org.spongycastle.util.encoders.Hex;

import static ethereumj.util.ByteUtil.byteArrayToInt;
import static ethereumj.util.ByteUtil.intToBytesNoLeadZeroes;
import static ethereumj.util.ByteUtil.isNullOrZeroArray;
import static ethereumj.util.ByteUtil.isSingleZero;
import static java.util.Arrays.copyOfRange;
import static org.spongycastle.util.Arrays.concatenate;
import static org.spongycastle.util.BigIntegers.asUnsignedByteArray;

public class RLP {

  private static final Logger logger = Logger.getLogger("rlp");
  private static final double MAX_ITEM_LENGTH = Math.pow(256, 8);
  private static final int SIZE_THRESHOLD = 56;
  private static final int OFFSET_SHORT_ITEM = 0x80;

  private static final int OFFSET_LONG_ITEM = 0xb7;
  public static final byte[] EMPTY_ELEMENT_RLP = encodeElement(new byte[0]);
  private static final int OFFSET_SHORT_LIST = 0xc0;

  private static final int OFFSET_LONG_LIST = 0xf7;

  private static byte decodeOneByteItem(byte[] data, int index) {
    if ((data[index] & 0xFF) == OFFSET_SHORT_ITEM) {
      return (byte) (data[index] - OFFSET_SHORT_ITEM);
    }
    if ((data[index] & 0xFF) < OFFSET_SHORT_ITEM) {
      return data[index];
    }
    if ((data[index] & 0xFF) == OFFSET_SHORT_ITEM + 1) {
      return data[index + 1];
    }
    return 0;
  }

  public static int decodeInt(byte[] data, int index) {
    int value = 0;

    if ((data[index] & 0xFF) < OFFSET_SHORT_ITEM) {
      return data[index];
    } else if ((data[index] & 0xFF) >= OFFSET_SHORT_ITEM
        && (data[index] & 0xFF) < OFFSET_LONG_ITEM) {

      byte length = (byte) (data[index] - OFFSET_SHORT_ITEM);
      byte pow = (byte) (length - 1);
      for (int i = 1; i <= length; ++i) {
        value += (data[index + i] & 0xFF) << (8 * pow);
        pow--;
      }
    } else {
      throw new RuntimeException("wrong decode attempt");
    }
    return value;
  }

  private static short decodeShort(byte[] data, int index) {
    if ((data[index] & 0xFF) > OFFSET_SHORT_ITEM && (data[index] & 0xFF) < OFFSET_LONG_ITEM) {
      byte length = (byte) (data[index] - OFFSET_SHORT_ITEM);
      return ByteBuffer.wrap(data, index, length)
          .getShort();
    } else {
      return data[index];
    }
  }

  private static long decodeLong(byte[] data, int index) {

    long value = 0;

    if ((data[index] & 0xFF) > OFFSET_SHORT_ITEM && (data[index] & 0xFF) < OFFSET_LONG_ITEM) {

      byte length = (byte) (data[index] - OFFSET_SHORT_ITEM);
      byte pow = (byte) (length - 1);
      for (int i = 1; i <= length; ++i) {
        value += (data[index + i] & 0xFF) << (8 * pow);
        pow--;
      }
    } else {
      throw new RuntimeException("wrong decode attempt");
    }
    return value;
  }

  private static String decodeStringItem(byte[] data, int index) {

    if ((data[index] & 0xFF) >= OFFSET_LONG_ITEM && (data[index] & 0xFF) < OFFSET_SHORT_LIST) {

      byte lengthOfLength = (byte) (data[index] - OFFSET_LONG_ITEM);
      int length = calcLengthRaw(lengthOfLength, data, index);
      return new String(data, index + lengthOfLength + 1, length);
    } else if ((data[index] & 0xFF) > OFFSET_SHORT_ITEM
        && (data[index] & 0xFF) < OFFSET_LONG_ITEM) {

      byte length = (byte) ((data[index] & 0xFF) - OFFSET_SHORT_ITEM);
      return new String(data, index + 1, length);
    } else {
      throw new RuntimeException("wrong decode attempt");
    }
  }

  private static byte[] decodeItemBytes(byte[] data, int index) {

    int length = calculateLength(data, index);
    byte[] valueBytes = new byte[length];
    System.arraycopy(data, index, valueBytes, 0, length);
    return valueBytes;
  }

  public static BigInteger decodeBigInteger(byte[] data, int index) {

    int length = calculateLength(data, index);
    byte[] valueBytes = new byte[length];
    System.arraycopy(data, index, valueBytes, 0, length);
    return new BigInteger(1, valueBytes);
  }

  private static byte[] decodeByteArray(byte[] data, int index) {

    int length = calculateLength(data, index);
    byte[] valueBytes = new byte[length];
    System.arraycopy(data, index, valueBytes, 0, length);
    return valueBytes;
  }

  private static int nextItemLength(byte[] data, int index) {

    if (index >= data.length) return -1;

    if ((data[index] & 0xFF) >= OFFSET_LONG_LIST) {
      byte lengthOfLength = (byte) (data[index] - OFFSET_LONG_LIST);

      return calcLength(lengthOfLength, data, index);
    }
    if ((data[index] & 0xFF) >= OFFSET_SHORT_LIST && (data[index] & 0xFF) < OFFSET_LONG_LIST) {

      return (byte) ((data[index] & 0xFF) - OFFSET_SHORT_LIST);
    }
    if ((data[index] & 0xFF) > OFFSET_LONG_ITEM && (data[index] & 0xFF) < OFFSET_SHORT_LIST) {

      byte lengthOfLength = (byte) (data[index] - OFFSET_LONG_ITEM);
      return calcLength(lengthOfLength, data, index);
    }
    if ((data[index] & 0xFF) > OFFSET_SHORT_ITEM && (data[index] & 0xFF) <= OFFSET_LONG_ITEM) {
      return (byte) ((data[index] & 0xFF) - OFFSET_SHORT_ITEM);
    }

    if ((data[index] & 0xFF) <= OFFSET_SHORT_ITEM) {
      return 1;
    }
    return -1;
  }

  public static byte[] decodeIP4Bytes(byte[] data, int index) {

    int offset = 1;

    byte[] result = new byte[4];
    for (int i = 0; i < 4; i++) {
      result[i] = decodeOneByteItem(data, index + offset);
      if ((data[index + offset] & 0xFF) > OFFSET_SHORT_ITEM) {
        offset += 2;
      } else {
        offset += 1;
      }
    }

    return result;
  }

  public static int getFirstListElement(byte[] payload, int pos) {

    if (pos >= payload.length) return -1;

    if ((payload[pos] & 0xFF) >= OFFSET_LONG_LIST) {
      byte lengthOfLength = (byte) (payload[pos] - OFFSET_LONG_LIST);
      return pos + lengthOfLength + 1;
    }
    if ((payload[pos] & 0xFF) >= OFFSET_SHORT_LIST && (payload[pos] & 0xFF) < OFFSET_LONG_LIST) {
      return pos + 1;
    }
    if ((payload[pos] & 0xFF) >= OFFSET_LONG_ITEM && (payload[pos] & 0xFF) < OFFSET_SHORT_LIST) {
      byte lengthOfLength = (byte) (payload[pos] - OFFSET_LONG_ITEM);
      return pos + lengthOfLength + 1;
    }
    return -1;
  }

  public static int getNextElementIndex(byte[] payload, int pos) {

    if (pos >= payload.length) return -1;

    if ((payload[pos] & 0xFF) >= OFFSET_LONG_LIST) {
      byte lengthOfLength = (byte) (payload[pos] - OFFSET_LONG_LIST);
      int length = calcLength(lengthOfLength, payload, pos);
      return pos + lengthOfLength + length + 1;
    }
    if ((payload[pos] & 0xFF) >= OFFSET_SHORT_LIST && (payload[pos] & 0xFF) < OFFSET_LONG_LIST) {

      byte length = (byte) ((payload[pos] & 0xFF) - OFFSET_SHORT_LIST);
      return pos + 1 + length;
    }
    if ((payload[pos] & 0xFF) >= OFFSET_LONG_ITEM && (payload[pos] & 0xFF) < OFFSET_SHORT_LIST) {

      byte lengthOfLength = (byte) (payload[pos] - OFFSET_LONG_ITEM);
      int length = calcLength(lengthOfLength, payload, pos);
      return pos + lengthOfLength + length + 1;
    }
    if ((payload[pos] & 0xFF) > OFFSET_SHORT_ITEM && (payload[pos] & 0xFF) < OFFSET_LONG_ITEM) {

      byte length = (byte) ((payload[pos] & 0xFF) - OFFSET_SHORT_ITEM);
      return pos + 1 + length;
    }
    if ((payload[pos] & 0xFF) == OFFSET_SHORT_ITEM) {
      return pos + 1;
    }
    if ((payload[pos] & 0xFF) < OFFSET_SHORT_ITEM) {
      return pos + 1;
    }
    return -1;
  }

  public static void fullTraverse(byte[] msgData, int level, int startPos, int endPos,
      int levelToIndex, Queue<Integer> index) {

    try {

      if (msgData == null || msgData.length == 0) return;
      int pos = startPos;

      while (pos < endPos) {

        if (level == levelToIndex) index.add(pos);

        if ((msgData[pos] & 0xFF) >= OFFSET_LONG_LIST) {

          byte lengthOfLength = (byte) (msgData[pos] - OFFSET_LONG_LIST);
          int length = calcLength(lengthOfLength, msgData, pos);

          System.out.println("-- level: [" + level + "] Found big list length: " + length);

          fullTraverse(msgData, level + 1, pos + lengthOfLength + 1, pos + lengthOfLength + length,
              levelToIndex, index);

          pos += lengthOfLength + length + 1;
          continue;
        }
        if ((msgData[pos] & 0xFF) >= OFFSET_SHORT_LIST
            && (msgData[pos] & 0xFF) < OFFSET_LONG_LIST) {

          byte length = (byte) ((msgData[pos] & 0xFF) - OFFSET_SHORT_LIST);

          System.out.println("-- level: [" + level + "] Found small list length: " + length);

          fullTraverse(msgData, level + 1, pos + 1, pos + length + 1, levelToIndex, index);

          pos += 1 + length;
          continue;
        }
        if ((msgData[pos] & 0xFF) >= OFFSET_LONG_ITEM
            && (msgData[pos] & 0xFF) < OFFSET_SHORT_LIST) {

          byte lengthOfLength = (byte) (msgData[pos] - OFFSET_LONG_ITEM);
          int length = calcLength(lengthOfLength, msgData, pos);

          System.out.println("-- level: [" + level + "] Found big item length: " + length);
          pos += lengthOfLength + length + 1;

          continue;
        }
        if ((msgData[pos] & 0xFF) > OFFSET_SHORT_ITEM && (msgData[pos] & 0xFF) < OFFSET_LONG_ITEM) {

          byte length = (byte) ((msgData[pos] & 0xFF) - OFFSET_SHORT_ITEM);

          System.out.println("-- level: [" + level + "] Found small item length: " + length);
          pos += 1 + length;
          continue;
        }
        if ((msgData[pos] & 0xFF) == OFFSET_SHORT_ITEM) {
          System.out.println("-- level: [" + level + "] Found null item: ");
          pos += 1;
          continue;
        }
        if ((msgData[pos] & 0xFF) < OFFSET_SHORT_ITEM) {
          System.out.println("-- level: [" + level + "] Found single item: ");
          pos += 1;
          continue;
        }
      }
    } catch (Throwable th) {
      throw new RuntimeException("RLP wrong encoding", th.fillInStackTrace());
    }
  }

  private static int calcLength(int lengthOfLength, byte[] msgData, int pos) {
    byte pow = (byte) (lengthOfLength - 1);
    int length = 0;
    for (int i = 1; i <= lengthOfLength; ++i) {
      length += (msgData[pos + i] & 0xFF) << (8 * pow);
      pow--;
    }
    return length;
  }

  private static int calcLengthRaw(int lengthOfLength, byte[] msgData, int index) {
    byte pow = (byte) (lengthOfLength - 1);
    int length = 0;
    for (int i = 1; i <= lengthOfLength; ++i) {
      length += msgData[index + i] << (8 * pow);
      pow--;
    }
    return length;
  }

  public static byte getCommandCode(byte[] data) {
    int index = getFirstListElement(data, 0);
    byte command = data[index];
    return ((command & 0xFF) == OFFSET_SHORT_ITEM) ? 0 : command;
  }

  public static RLPList decode2(byte[] msgData) {
    RLPList rlpList = new RLPList();
    fullTraverse(msgData, 0, 0, msgData.length, 1, rlpList);
    return rlpList;
  }

  public static RLPElement decode2OneItem(byte[] msgData, int startPos) {
    RLPList rlpList = new RLPList();
    fullTraverse(msgData, 0, startPos, startPos + 1, 1, rlpList);
    return rlpList.get(0);
  }

  private static void fullTraverse(byte[] msgData, int level, int startPos, int endPos,
      int levelToIndex, RLPList rlpList) {

    try {
      if (msgData == null || msgData.length == 0) return;
      int pos = startPos;

      while (pos < endPos) {

        logger.info("fullTraverse: level: " + level + " startPos: " + pos + " endPos: " + endPos);

        if ((msgData[pos] & 0xFF) > OFFSET_LONG_LIST) {

          byte lengthOfLength = (byte) (msgData[pos] - OFFSET_LONG_LIST);
          int length = calcLength(lengthOfLength, msgData, pos);

          byte[] rlpData = new byte[lengthOfLength + length + 1];
          System.arraycopy(msgData, pos, rlpData, 0, lengthOfLength + length + 1);

          RLPList newLevelList = new RLPList();
          newLevelList.setRLPData(rlpData);

          fullTraverse(msgData, level + 1, pos + lengthOfLength + 1,
              pos + lengthOfLength + length + 1, levelToIndex, newLevelList);
          rlpList.add(newLevelList);

          pos += lengthOfLength + length + 1;
          continue;
        }
        if ((msgData[pos] & 0xFF) >= OFFSET_SHORT_LIST
            && (msgData[pos] & 0xFF) <= OFFSET_LONG_LIST) {

          byte length = (byte) ((msgData[pos] & 0xFF) - OFFSET_SHORT_LIST);

          byte[] rlpData = new byte[length + 1];
          System.arraycopy(msgData, pos, rlpData, 0, length + 1);

          RLPList newLevelList = new RLPList();
          newLevelList.setRLPData(rlpData);

          if (length > 0) {
            fullTraverse(msgData, level + 1, pos + 1, pos + length + 1, levelToIndex, newLevelList);
          }
          rlpList.add(newLevelList);

          pos += 1 + length;
          continue;
        }
        if ((msgData[pos] & 0xFF) > OFFSET_LONG_ITEM && (msgData[pos] & 0xFF) < OFFSET_SHORT_LIST) {

          byte lengthOfLength = (byte) (msgData[pos] - OFFSET_LONG_ITEM);
          int length = calcLength(lengthOfLength, msgData, pos);

          byte[] item = new byte[length];
          System.arraycopy(msgData, pos + lengthOfLength + 1, item, 0, length);

          byte[] rlpPrefix = new byte[lengthOfLength + 1];
          System.arraycopy(msgData, pos, rlpPrefix, 0, lengthOfLength + 1);

          RLPItem rlpItem = new RLPItem(item);
          rlpList.add(rlpItem);
          pos += lengthOfLength + length + 1;

          continue;
        }
        if ((msgData[pos] & 0xFF) > OFFSET_SHORT_ITEM
            && (msgData[pos] & 0xFF) <= OFFSET_LONG_ITEM) {

          byte length = (byte) ((msgData[pos] & 0xFF) - OFFSET_SHORT_ITEM);

          byte[] item = new byte[length];
          System.arraycopy(msgData, pos + 1, item, 0, length);

          byte[] rlpPrefix = new byte[2];
          System.arraycopy(msgData, pos, rlpPrefix, 0, 2);

          RLPItem rlpItem = new RLPItem(item);
          rlpList.add(rlpItem);
          pos += 1 + length;

          continue;
        }
        if ((msgData[pos] & 0xFF) == OFFSET_SHORT_ITEM) {
          byte[] item = ByteUtil.EMPTY_BYTE_ARRAY;
          RLPItem rlpItem = new RLPItem(item);
          rlpList.add(rlpItem);
          pos += 1;
          continue;
        }
        if ((msgData[pos] & 0xFF) < OFFSET_SHORT_ITEM) {

          byte[] item = { (byte) (msgData[pos] & 0xFF) };

          RLPItem rlpItem = new RLPItem(item);
          rlpList.add(rlpItem);
          pos += 1;
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(
          "RLP wrong encoding (" + Hex.toHexString(msgData, startPos, endPos - startPos) + ")", e);
    } catch (OutOfMemoryError e) {
      throw new RuntimeException(
          "Invalid RLP (excessive mem allocation while parsing) (" + Hex.toHexString(msgData,
              startPos, endPos - startPos) + ")", e);
    }
  }

  public static DecodeResult decode(byte[] data, int pos) {
    if (data == null || data.length < 1) {
      return null;
    }
    int prefix = data[pos] & 0xFF;
    if (prefix == OFFSET_SHORT_ITEM) {
      return new DecodeResult(pos + 1, "");
    } else if (prefix < OFFSET_SHORT_ITEM) {
      return new DecodeResult(pos + 1, new byte[] { data[pos] });
    } else if (prefix <= OFFSET_LONG_ITEM) {
      int len = prefix - OFFSET_SHORT_ITEM;
      return new DecodeResult(pos + 1 + len, copyOfRange(data, pos + 1, pos + 1 + len));
    } else if (prefix < OFFSET_SHORT_LIST) {
      int lenlen = prefix - OFFSET_LONG_ITEM;
      int lenbytes = byteArrayToInt(copyOfRange(data, pos + 1, pos + 1 + lenlen));
      return new DecodeResult(pos + 1 + lenlen + lenbytes,
          copyOfRange(data, pos + 1 + lenlen, pos + 1 + lenlen + lenbytes));
    } else if (prefix <= OFFSET_LONG_LIST) {
      int len = prefix - OFFSET_SHORT_LIST;
      int prevPos = pos;
      pos++;
      return decodeList(data, pos, prevPos, len);
    } else if (prefix <= 0xFF) {
      int lenlen = prefix - OFFSET_LONG_LIST;
      int lenlist = byteArrayToInt(copyOfRange(data, pos + 1, pos + 1 + lenlen));
      pos = pos + lenlen + 1;
      int prevPos = lenlist;
      return decodeList(data, pos, prevPos, lenlist);
    } else {
      throw new RuntimeException(
          "Only byte values between 0x00 and 0xFF are supported, but got: " + prefix);
    }
  }

  public static LList decodeLazyList(byte[] data) {
    return decodeLazyList(data, 0, data.length).getList(0);
  }

  public static LList decodeLazyList(byte[] data, int pos, int length) {
    if (data == null || data.length < 1) {
      return null;
    }
    LList ret = new LList(data);
    int end = pos + length;

    while (pos < end) {
      int prefix = data[pos] & 0xFF;
      if (prefix == OFFSET_SHORT_ITEM) {
        ret.add(pos, 0, false);
        pos++;
      } else if (prefix < OFFSET_SHORT_ITEM) {
        ret.add(pos, 1, false);
        pos++;
      } else if (prefix <= OFFSET_LONG_ITEM) {
        int len = prefix - OFFSET_SHORT_ITEM;
        ret.add(pos + 1, len, false);
        pos += len + 1;
      } else if (prefix < OFFSET_SHORT_LIST) {
        int lenlen = prefix - OFFSET_LONG_ITEM;
        int lenbytes = byteArrayToInt(copyOfRange(data, pos + 1, pos + 1 + lenlen));
        ret.add(pos + 1 + lenlen, lenbytes, false);
        pos += 1 + lenlen + lenbytes;
      } else if (prefix <= OFFSET_LONG_LIST) {
        int len = prefix - OFFSET_SHORT_LIST;
        ret.add(pos + 1, len, true);
        pos += 1 + len;
      } else if (prefix <= 0xFF) {
        int lenlen = prefix - OFFSET_LONG_LIST;
        int lenlist = byteArrayToInt(copyOfRange(data, pos + 1, pos + 1 + lenlen));
        ret.add(pos + 1 + lenlen, lenlist, true);
        pos += 1 + lenlen + lenlist;
      } else {
        throw new RuntimeException(
            "Only byte values between 0x00 and 0xFF are supported, but got: " + prefix);
      }
    }
    return ret;
  }

  private static DecodeResult decodeList(byte[] data, int pos, int prevPos, int len) {
    List<Object> slice = new ArrayList<>();
    for (int i = 0; i < len; ) {
      DecodeResult result = decode(data, pos);
      slice.add(result.getDecoded());
      prevPos = result.getPos();
      i += (prevPos - pos);
      pos = prevPos;
    }
    return new DecodeResult(pos, slice.toArray());
  }

  public static byte[] encode(Object input) {
    Value val = new Value(input);
    if (val.isList()) {
      List<Object> inputArray = val.asList();
      if (inputArray.isEmpty()) {
        return encodeLength(inputArray.size(), OFFSET_SHORT_LIST);
      }
      byte[] output = ByteUtil.EMPTY_BYTE_ARRAY;
      for (Object object : inputArray) {
        output = concatenate(output, encode(object));
      }
      byte[] prefix = encodeLength(output.length, OFFSET_SHORT_LIST);
      return concatenate(prefix, output);
    } else {
      byte[] inputAsBytes = toBytes(input);
      if (inputAsBytes.length == 1 && (inputAsBytes[0] & 0xff) <= 0x80) {
        return inputAsBytes;
      } else {
        byte[] firstByte = encodeLength(inputAsBytes.length, OFFSET_SHORT_ITEM);
        return concatenate(firstByte, inputAsBytes);
      }
    }
  }

  public static byte[] encodeLength(int length, int offset) {
    if (length < SIZE_THRESHOLD) {
      byte firstByte = (byte) (length + offset);
      return new byte[] { firstByte };
    } else if (length < MAX_ITEM_LENGTH) {
      byte[] binaryLength;
      if (length > 0xFF) {
        binaryLength = intToBytesNoLeadZeroes(length);
      } else {
        binaryLength = new byte[] { (byte) length };
      }
      byte firstByte = (byte) (binaryLength.length + offset + SIZE_THRESHOLD - 1);
      return concatenate(new byte[] { firstByte }, binaryLength);
    } else {
      throw new RuntimeException("Input too long");
    }
  }

  public static byte[] encodeByte(byte singleByte) {
    if ((singleByte & 0xFF) == 0) {
      return new byte[] { (byte) OFFSET_SHORT_ITEM };
    } else if ((singleByte & 0xFF) <= 0x7F) {
      return new byte[] { singleByte };
    } else {
      return new byte[] { (byte) (OFFSET_SHORT_ITEM + 1), singleByte };
    }
  }

  public static byte[] encodeShort(short singleShort) {
    if ((singleShort & 0xFF) == singleShort) {
      return encodeByte((byte) singleShort);
    } else {
      return new byte[] {
          (byte) (OFFSET_SHORT_ITEM + 2), (byte) (singleShort >> 8 & 0xFF),
          (byte) (singleShort >> 0 & 0xFF)
      };
    }
  }

  public static byte[] encodeInt(int singleInt) {
    if ((singleInt & 0xFF) == singleInt) {
      return encodeByte((byte) singleInt);
    } else if ((singleInt & 0xFFFF) == singleInt) {
      return encodeShort((short) singleInt);
    } else if ((singleInt & 0xFFFFFF) == singleInt) {
      return new byte[] {
          (byte) (OFFSET_SHORT_ITEM + 3), (byte) (singleInt >>> 16), (byte) (singleInt >>> 8),
          (byte) singleInt
      };
    } else {
      return new byte[] {
          (byte) (OFFSET_SHORT_ITEM + 4), (byte) (singleInt >>> 24), (byte) (singleInt >>> 16),
          (byte) (singleInt >>> 8), (byte) singleInt
      };
    }
  }

  public static byte[] encodeString(String srcString) {
    return encodeElement(srcString.getBytes());
  }

  public static byte[] encodeBigInteger(BigInteger srcBigInteger) {
    if (srcBigInteger.equals(BigInteger.ZERO)) {
      return encodeByte((byte) 0);
    } else {
      return encodeElement(asUnsignedByteArray(srcBigInteger));
    }
  }

  public static byte[] encodeElement(byte[] srcData) {

    if (isNullOrZeroArray(srcData)) {
      return new byte[] { (byte) OFFSET_SHORT_ITEM };
    } else if (isSingleZero(srcData)) {
      return srcData;
    } else if (srcData.length == 1 && (srcData[0] & 0xFF) < 0x80) {
      return srcData;
    } else if (srcData.length < SIZE_THRESHOLD) {
      byte length = (byte) (OFFSET_SHORT_ITEM + srcData.length);
      byte[] data = Arrays.copyOf(srcData, srcData.length + 1);
      System.arraycopy(data, 0, data, 1, srcData.length);
      data[0] = length;

      return data;
    } else {
      int tmpLength = srcData.length;
      byte byteNum = 0;
      while (tmpLength != 0) {
        ++byteNum;
        tmpLength = tmpLength >> 8;
      }
      byte[] lenBytes = new byte[byteNum];
      for (int i = 0; i < byteNum; ++i) {
        lenBytes[byteNum - 1 - i] = (byte) ((srcData.length >> (8 * i)) & 0xFF);
      }
      byte[] data = Arrays.copyOf(srcData, srcData.length + 1 + byteNum);
      System.arraycopy(data, 0, data, 1 + byteNum, srcData.length);
      data[0] = (byte) (OFFSET_LONG_ITEM + byteNum);
      System.arraycopy(lenBytes, 0, data, 1, lenBytes.length);

      return data;
    }
  }

  public static int calcElementPrefixSize(byte[] srcData) {

    if (isNullOrZeroArray(srcData)) {
      return 0;
    } else if (isSingleZero(srcData)) {
      return 0;
    } else if (srcData.length == 1 && (srcData[0] & 0xFF) < 0x80) {
      return 0;
    } else if (srcData.length < SIZE_THRESHOLD) {
      return 1;
    } else {
      int tmpLength = srcData.length;
      byte byteNum = 0;
      while (tmpLength != 0) {
        ++byteNum;
        tmpLength = tmpLength >> 8;
      }

      return 1 + byteNum;
    }
  }

  public static byte[] encodeListHeader(int size) {

    if (size == 0) {
      return new byte[] { (byte) OFFSET_SHORT_LIST };
    }

    int totalLength = size;

    byte[] header;
    if (totalLength < SIZE_THRESHOLD) {

      header = new byte[1];
      header[0] = (byte) (OFFSET_SHORT_LIST + totalLength);
    } else {
      int tmpLength = totalLength;
      byte byteNum = 0;
      while (tmpLength != 0) {
        ++byteNum;
        tmpLength = tmpLength >> 8;
      }
      tmpLength = totalLength;

      byte[] lenBytes = new byte[byteNum];
      for (int i = 0; i < byteNum; ++i) {
        lenBytes[byteNum - 1 - i] = (byte) ((tmpLength >> (8 * i)) & 0xFF);
      }
      header = new byte[1 + lenBytes.length];
      header[0] = (byte) (OFFSET_LONG_LIST + byteNum);
      System.arraycopy(lenBytes, 0, header, 1, lenBytes.length);
    }

    return header;
  }

  public static byte[] encodeLongElementHeader(int length) {

    if (length < SIZE_THRESHOLD) {

      if (length == 0) {
        return new byte[] { (byte) 0x80 };
      } else {
        return new byte[] { (byte) (0x80 + length) };
      }
    } else {
      int tmpLength = length;
      byte byteNum = 0;
      while (tmpLength != 0) {
        ++byteNum;
        tmpLength = tmpLength >> 8;
      }

      byte[] lenBytes = new byte[byteNum];
      for (int i = 0; i < byteNum; ++i) {
        lenBytes[byteNum - 1 - i] = (byte) ((length >> (8 * i)) & 0xFF);
      }
      byte[] header = new byte[1 + lenBytes.length];
      header[0] = (byte) (OFFSET_LONG_ITEM + byteNum);
      System.arraycopy(lenBytes, 0, header, 1, lenBytes.length);

      return header;
    }
  }

  public static byte[] encodeSet(Set<ByteArrayWrapper> data) {

    int dataLength = 0;
    Set<byte[]> encodedElements = new HashSet<>();
    for (ByteArrayWrapper element : data) {

      byte[] encodedElement = encodeElement(element.getData());
      dataLength += encodedElement.length;
      encodedElements.add(encodedElement);
    }

    byte[] listHeader = encodeListHeader(dataLength);

    byte[] output = new byte[listHeader.length + dataLength];

    System.arraycopy(listHeader, 0, output, 0, listHeader.length);

    int cummStart = listHeader.length;
    for (byte[] element : encodedElements) {
      System.arraycopy(element, 0, output, cummStart, element.length);
      cummStart += element.length;
    }

    return output;
  }

  public static byte[] encodeList(byte[]... elements) {

    if (elements == null) {
      return new byte[] { (byte) OFFSET_SHORT_LIST };
    }

    int totalLength = 0;
    for (byte[] element1 : elements) {
      totalLength += element1.length;
    }

    byte[] data;
    int copyPos;
    if (totalLength < SIZE_THRESHOLD) {

      data = new byte[1 + totalLength];
      data[0] = (byte) (OFFSET_SHORT_LIST + totalLength);
      copyPos = 1;
    } else {
      int tmpLength = totalLength;
      byte byteNum = 0;
      while (tmpLength != 0) {
        ++byteNum;
        tmpLength = tmpLength >> 8;
      }
      tmpLength = totalLength;
      byte[] lenBytes = new byte[byteNum];
      for (int i = 0; i < byteNum; ++i) {
        lenBytes[byteNum - 1 - i] = (byte) ((tmpLength >> (8 * i)) & 0xFF);
      }
      data = new byte[1 + lenBytes.length + totalLength];
      data[0] = (byte) (OFFSET_LONG_LIST + byteNum);
      System.arraycopy(lenBytes, 0, data, 1, lenBytes.length);

      copyPos = lenBytes.length + 1;
    }
    for (byte[] element : elements) {
      System.arraycopy(element, 0, data, copyPos, element.length);
      copyPos += element.length;
    }
    return data;
  }

  private static byte[] toBytes(Object input) {
    if (input instanceof byte[]) {
      return (byte[]) input;
    } else if (input instanceof String) {
      String inputString = (String) input;
      return inputString.getBytes();
    } else if (input instanceof Long) {
      Long inputLong = (Long) input;
      return (inputLong == 0) ? ByteUtil.EMPTY_BYTE_ARRAY
          : asUnsignedByteArray(BigInteger.valueOf(inputLong));
    } else if (input instanceof Integer) {
      Integer inputInt = (Integer) input;
      return (inputInt == 0) ? ByteUtil.EMPTY_BYTE_ARRAY
          : asUnsignedByteArray(BigInteger.valueOf(inputInt));
    } else if (input instanceof BigInteger) {
      BigInteger inputBigInt = (BigInteger) input;
      return (inputBigInt.equals(BigInteger.ZERO)) ? ByteUtil.EMPTY_BYTE_ARRAY
          : asUnsignedByteArray(inputBigInt);
    } else if (input instanceof Value) {
      Value val = (Value) input;
      return toBytes(val.asObj());
    }
    throw new RuntimeException(
        "Unsupported type: Only accepting String, Integer and BigInteger for now");
  }

  private static int calculateLength(byte[] data, int index) {
    if ((data[index] & 0xFF) >= OFFSET_LONG_ITEM && (data[index] & 0xFF) < OFFSET_SHORT_LIST) {

      byte lengthOfLength = (byte) (data[index] - OFFSET_LONG_ITEM);
      return calcLengthRaw(lengthOfLength, data, index);
    } else if ((data[index] & 0xFF) > OFFSET_SHORT_ITEM
        && (data[index] & 0xFF) < OFFSET_LONG_ITEM) {

      return (byte) (data[index] - OFFSET_SHORT_ITEM);
    } else {
      throw new RuntimeException("wrong decode attempt");
    }
  }

  public static final class LList {
    private final byte[] rlp;
    private final int[] offsets = new int[32];
    private final int[] lens = new int[32];
    private int cnt;

    public LList(byte[] rlp) {
      this.rlp = rlp;
    }

    public void add(int off, int len, boolean isList) {
      offsets[cnt] = off;
      lens[cnt] = isList ? (-1 - len) : len;
      cnt++;
    }

    public byte[] getBytes(int idx) {
      int len = lens[idx];
      len = len < 0 ? (-len - 1) : len;
      byte[] ret = new byte[len];
      System.arraycopy(rlp, offsets[idx], ret, 0, len);
      return ret;
    }

    public LList getList(int idx) {
      return decodeLazyList(rlp, offsets[idx], -lens[idx] - 1);
    }

    public boolean isList(int idx) {
      return lens[idx] < 0;
    }

    public int size() {
      return cnt;
    }
  }
}
