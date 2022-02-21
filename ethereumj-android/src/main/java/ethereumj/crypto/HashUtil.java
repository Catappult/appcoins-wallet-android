package ethereumj.crypto;

import ethereumj.crypto.cryptohash.Keccak256;
import ethereumj.crypto.cryptohash.Keccak512;
import ethereumj.util.RLP;
import ethereumj.util.Utils;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import org.spongycastle.crypto.Digest;
import org.spongycastle.crypto.digests.RIPEMD160Digest;
import org.spongycastle.util.encoders.Hex;

import static ethereumj.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static java.util.Arrays.copyOfRange;

public class HashUtil {

  public static final byte[] EMPTY_DATA_HASH = sha3(EMPTY_BYTE_ARRAY);
  public static final byte[] EMPTY_LIST_HASH = sha3(RLP.encodeList());
  public static final byte[] EMPTY_TRIE_HASH = sha3(RLP.encodeElement(EMPTY_BYTE_ARRAY));
  private static final MessageDigest sha256digest;

  static {
    try {
      sha256digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) {
    System.out.println();
  }

  public static byte[] sha256(byte[] input) {
    return sha256digest.digest(input);
  }

  public static byte[] sha3(byte[] input) {
    Keccak256 digest = new Keccak256();
    digest.update(input);
    return digest.digest();
  }

  public static byte[] sha3(byte[] input1, byte[] input2) {
    Keccak256 digest = new Keccak256();
    digest.update(input1, 0, input1.length);
    digest.update(input2, 0, input2.length);
    return digest.digest();
  }

  public static byte[] sha3(byte[] input, int start, int length) {
    Keccak256 digest = new Keccak256();
    digest.update(input, start, length);
    return digest.digest();
  }

  public static byte[] sha512(byte[] input) {
    Keccak512 digest = new Keccak512();
    digest.update(input);
    return digest.digest();
  }

  public static byte[] ripemd160(byte[] data) {
    Digest digest = new RIPEMD160Digest();
    if (data != null) {
      byte[] resBuf = new byte[digest.getDigestSize()];
      digest.update(data, 0, data.length);
      digest.doFinal(resBuf, 0);
      return resBuf;
    }
    throw new NullPointerException("Can't hash a NULL value");
  }

  public static byte[] sha3omit12(byte[] input) {
    byte[] hash = sha3(input);
    return copyOfRange(hash, 12, hash.length);
  }

  public static byte[] calcNewAddr(byte[] addr, byte[] nonce) {

    byte[] encSender = RLP.encodeElement(addr);
    byte[] encNonce = RLP.encodeBigInteger(new BigInteger(1, nonce));

    return sha3omit12(RLP.encodeList(encSender, encNonce));
  }

  public static byte[] doubleDigest(byte[] input) {
    return doubleDigest(input, 0, input.length);
  }

  public static byte[] doubleDigest(byte[] input, int offset, int length) {
    synchronized (sha256digest) {
      sha256digest.reset();
      sha256digest.update(input, offset, length);
      byte[] first = sha256digest.digest();
      return sha256digest.digest(first);
    }
  }

  public static byte[] randomPeerId() {

    byte[] peerIdBytes = new BigInteger(512, Utils.getRandom()).toByteArray();

    String peerId;
    if (peerIdBytes.length > 64) {
      peerId = Hex.toHexString(peerIdBytes, 1, 64);
    } else {
      peerId = Hex.toHexString(peerIdBytes);
    }

    return Hex.decode(peerId);
  }

  public static byte[] randomHash() {

    byte[] randomHash = new byte[32];
    Random random = new Random();
    random.nextBytes(randomHash);
    return randomHash;
  }

  public static String shortHash(byte[] hash) {
    return Hex.toHexString(hash)
        .substring(0, 6);
  }
}
