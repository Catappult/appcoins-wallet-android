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

  public static byte[] sha3omit12(byte[] input) {
    byte[] hash = sha3(input);
    return copyOfRange(hash, 12, hash.length);
  }

  public static byte[] calcNewAddr(byte[] addr, byte[] nonce) {

    byte[] encSender = RLP.encodeElement(addr);
    byte[] encNonce = RLP.encodeBigInteger(new BigInteger(1, nonce));

    return sha3omit12(RLP.encodeList(encSender, encNonce));
  }
}
