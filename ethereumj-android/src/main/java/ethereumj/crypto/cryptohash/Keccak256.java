package ethereumj.crypto.cryptohash;

public class Keccak256 extends KeccakCore {

  public Keccak256() {
  }

  public int getDigestLength() {
    return 32;
  }

  public Digest copy() {
    return copyState(new Keccak256());
  }
}
