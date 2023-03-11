package ethereumj.crypto.cryptohash;

public class Keccak512 extends KeccakCore {

  public Keccak512() {
  }

  public int getDigestLength() {
    return 64;
  }

  public Digest copy() {
    return copyState(new Keccak512());
  }
}
