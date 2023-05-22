package ethereumj.crypto.cryptohash;

public abstract class DigestEngine implements Digest {

  private final int blockLen;
  private final byte[] inputBuf;
  private int digestLen;
  private int inputLen;
  private byte[] outputBuf;
  private long blockCount;

  public DigestEngine() {
    doInit();
    digestLen = getDigestLength();
    blockLen = getInternalBlockLength();
    inputBuf = new byte[blockLen];
    outputBuf = new byte[digestLen];
    inputLen = 0;
    blockCount = 0;
  }

  protected abstract void engineReset();

  protected abstract void processBlock(byte[] data);

  protected abstract void doPadding(byte[] buf, int off);

  protected abstract void doInit();

  private void adjustDigestLen() {
    if (digestLen == 0) {
      digestLen = getDigestLength();
      outputBuf = new byte[digestLen];
    }
  }

  public void update(byte input) {
    inputBuf[inputLen++] = input;
    if (inputLen == blockLen) {
      processBlock(inputBuf);
      blockCount++;
      inputLen = 0;
    }
  }

  public void update(byte[] input) {
    update(input, 0, input.length);
  }

  public void update(byte[] input, int offset, int len) {
    while (len > 0) {
      int copyLen = blockLen - inputLen;
      if (copyLen > len) copyLen = len;
      System.arraycopy(input, offset, inputBuf, inputLen, copyLen);
      offset += copyLen;
      inputLen += copyLen;
      len -= copyLen;
      if (inputLen == blockLen) {
        processBlock(inputBuf);
        blockCount++;
        inputLen = 0;
      }
    }
  }

  public byte[] digest() {
    adjustDigestLen();
    byte[] result = new byte[digestLen];
    digest(result, 0, digestLen);
    return result;
  }

  public byte[] digest(byte[] input) {
    update(input, 0, input.length);
    return digest();
  }

  public int digest(byte[] buf, int offset, int len) {
    adjustDigestLen();
    if (len >= digestLen) {
      doPadding(buf, offset);
      reset();
      return digestLen;
    } else {
      doPadding(outputBuf, 0);
      System.arraycopy(outputBuf, 0, buf, offset, len);
      reset();
      return len;
    }
  }

  public void reset() {
    engineReset();
    inputLen = 0;
    blockCount = 0;
  }

  protected int getInternalBlockLength() {
    return getBlockLength();
  }

  protected final int flush() {
    return inputLen;
  }

  protected final byte[] getBlockBuffer() {
    return inputBuf;
  }

  protected long getBlockCount() {
    return blockCount;
  }

  protected Digest copyState(DigestEngine dest) {
    dest.inputLen = inputLen;
    dest.blockCount = blockCount;
    System.arraycopy(inputBuf, 0, dest.inputBuf, 0, inputBuf.length);
    adjustDigestLen();
    dest.adjustDigestLen();
    System.arraycopy(outputBuf, 0, dest.outputBuf, 0, outputBuf.length);
    return dest;
  }
}
