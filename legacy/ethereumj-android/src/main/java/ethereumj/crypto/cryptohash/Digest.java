package ethereumj.crypto.cryptohash;

public interface Digest {

  void update(byte in);

  void update(byte[] inbuf);

  void update(byte[] inbuf, int off, int len);

  byte[] digest();

  byte[] digest(byte[] inbuf);

  int digest(byte[] outbuf, int off, int len);

  int getDigestLength();

  void reset();

  Digest copy();

  int getBlockLength();

  String toString();
}
