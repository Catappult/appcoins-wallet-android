package ethereumj.crypto;

import ethereumj.crypto.cryptohash.Keccak256;
import ethereumj.util.RLP;
import java.math.BigInteger;

import static java.util.Arrays.copyOfRange;

public class HashUtil {

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
