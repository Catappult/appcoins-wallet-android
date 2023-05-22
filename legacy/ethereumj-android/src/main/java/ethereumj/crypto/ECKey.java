package ethereumj.crypto;

import androidx.annotation.Nullable;
import ethereumj.config.Constants;
import ethereumj.crypto.jce.ECKeyFactory;
import ethereumj.crypto.jce.ECKeyPairGenerator;
import ethereumj.crypto.jce.ECSignatureFactory;
import ethereumj.crypto.jce.SpongyCastleProvider;
import ethereumj.util.ByteUtil;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.DLSequence;
import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.asn1.x9.X9IntegerConverter;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.jce.spec.ECPrivateKeySpec;
import org.spongycastle.math.ec.ECAlgorithms;
import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.math.ec.ECPoint.Fp;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import static ethereumj.util.BIUtil.isLessThan;
import static ethereumj.util.ByteUtil.bigIntegerToBytes;

public class ECKey implements Serializable {

  public static final ECDomainParameters CURVE;
  public static final ECParameterSpec CURVE_SPEC;

  public static final BigInteger HALF_CURVE_ORDER;

  private static final SecureRandom secureRandom;
  private static final long serialVersionUID = -728224901792295832L;

  static {
    X9ECParameters params = SECNamedCurves.getByName("secp256k1");
    CURVE = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
    CURVE_SPEC =
        new ECParameterSpec(params.getCurve(), params.getG(), params.getN(), params.getH());
    HALF_CURVE_ORDER = params.getN()
        .shiftRight(1);
    secureRandom = new SecureRandom();
  }

  protected final ECPoint pub;
  private final PrivateKey privKey;
  private final Provider provider;

  private transient byte[] pubKeyHash;
  private transient byte[] nodeId;

  public ECKey() {
    this(secureRandom);
  }

  public ECKey(Provider provider, SecureRandom secureRandom) {
    this.provider = provider;

    KeyPairGenerator keyPairGen = ECKeyPairGenerator.getInstance(provider, secureRandom);
    KeyPair keyPair = keyPairGen.generateKeyPair();

    this.privKey = keyPair.getPrivate();
    PublicKey pubKey = keyPair.getPublic();

    if (pubKey instanceof BCECPublicKey) {
      pub = ((BCECPublicKey) pubKey).getQ();
    } else if (pubKey instanceof ECPublicKey) {
      pub = extractPublicKey((ECPublicKey) pubKey);
    } else {
      throw new AssertionError("Expected Provider "
          + provider.getName()
          + " to produce a subtype of ECPublicKey, found "
          + pubKey.getClass());
    }
  }

  public ECKey(SecureRandom secureRandom) {
    this(SpongyCastleProvider.getInstance(), secureRandom);
  }

  public ECKey(Provider provider, @Nullable PrivateKey privKey, ECPoint pub) {
    this.provider = provider;

    if (privKey == null || isECPrivateKey(privKey)) {
      this.privKey = privKey;
    } else {
      throw new IllegalArgumentException(
          "Expected EC private key, given a private key object with class "
              + privKey.getClass()
              + " and algorithm "
              + privKey.getAlgorithm());
    }

    if (pub == null) {
      throw new IllegalArgumentException("Public key may not be null");
    } else {
      this.pub = pub;
    }
  }

  public ECKey(@Nullable BigInteger priv, ECPoint pub) {
    this(SpongyCastleProvider.getInstance(), privateKeyFromBigInteger(priv), pub);
  }

  private static ECPoint extractPublicKey(ECPublicKey ecPublicKey) {
    java.security.spec.ECPoint publicPointW = ecPublicKey.getW();
    BigInteger xCoord = publicPointW.getAffineX();
    BigInteger yCoord = publicPointW.getAffineY();

    return CURVE.getCurve()
        .createPoint(xCoord, yCoord);
  }

  private static boolean isECPrivateKey(PrivateKey privKey) {
    return privKey instanceof ECPrivateKey || privKey.getAlgorithm()
        .equals("EC");
  }

  private static PrivateKey privateKeyFromBigInteger(BigInteger priv) {
    if (priv == null) {
      return null;
    } else {
      try {
        return ECKeyFactory.getInstance(SpongyCastleProvider.getInstance())
            .generatePrivate(new ECPrivateKeySpec(priv, CURVE_SPEC));
      } catch (InvalidKeySpecException ex) {
        throw new AssertionError("Assumed correct key spec statically");
      }
    }
  }

  public static ECKey fromPrivate(BigInteger privKey) {
    return new ECKey(privKey, CURVE.getG()
        .multiply(privKey));
  }

  public static ECKey fromPrivate(byte[] privKeyBytes) {
    return fromPrivate(new BigInteger(1, privKeyBytes));
  }

  public static ECKey fromPrivateAndPrecalculatedPublic(BigInteger priv, ECPoint pub) {
    return new ECKey(priv, pub);
  }

  public static ECKey fromPrivateAndPrecalculatedPublic(byte[] priv, byte[] pub) {
    check(priv != null, "Private key must not be null");
    check(pub != null, "Public key must not be null");
    return new ECKey(new BigInteger(1, priv), CURVE.getCurve()
        .decodePoint(pub));
  }

  public static ECKey fromPublicOnly(ECPoint pub) {
    return new ECKey(null, pub);
  }

  public static ECKey fromPublicOnly(byte[] pub) {
    return new ECKey(null, CURVE.getCurve()
        .decodePoint(pub));
  }

  public static byte[] publicKeyFromPrivate(BigInteger privKey, boolean compressed) {
    ECPoint point = CURVE.getG()
        .multiply(privKey);
    return point.getEncoded(compressed);
  }

  public static byte[] computeAddress(byte[] pubBytes) {
    return HashUtil.sha3omit12(Arrays.copyOfRange(pubBytes, 1, pubBytes.length));
  }

  public static byte[] computeAddress(ECPoint pubPoint) {
    return computeAddress(pubPoint.getEncoded(/* uncompressed */ false));
  }

  public static byte[] pubBytesWithoutFormat(ECPoint pubPoint) {
    byte[] pubBytes = pubPoint.getEncoded(/* uncompressed */ false);
    return Arrays.copyOfRange(pubBytes, 1, pubBytes.length);
  }

  public static ECKey fromNodeId(byte[] nodeId) {
    check(nodeId.length == 64, "Expected a 64 byte node id");
    byte[] pubBytes = new byte[65];
    System.arraycopy(nodeId, 0, pubBytes, 1, nodeId.length);
    pubBytes[0] = 0x04;
    return fromPublicOnly(pubBytes);
  }

  public static byte[] signatureToKeyBytes(byte[] messageHash, String signatureBase64)
      throws SignatureException {
    byte[] signatureEncoded;
    try {
      signatureEncoded = Base64.decode(signatureBase64);
    } catch (RuntimeException e) {
      throw new SignatureException("Could not decode base64", e);
    }
    if (signatureEncoded.length < 65) {
      throw new SignatureException(
          "Signature truncated, expected 65 bytes and got " + signatureEncoded.length);
    }

    return signatureToKeyBytes(messageHash,
        ECDSASignature.fromComponents(Arrays.copyOfRange(signatureEncoded, 1, 33),
            Arrays.copyOfRange(signatureEncoded, 33, 65), (byte) (signatureEncoded[0] & 0xFF)));
  }

  public static byte[] signatureToKeyBytes(byte[] messageHash, ECDSASignature sig)
      throws SignatureException {
    check(messageHash.length == 32, "messageHash argument has length " + messageHash.length);
    int header = sig.v;
    if (header < 27 || header > 34) {
      throw new SignatureException("Header byte out of range: " + header);
    }
    if (header >= 31) {
      header -= 4;
    }
    int recId = header - 27;
    byte[] key = recoverPubBytesFromSignature(recId, sig, messageHash);
    if (key == null) throw new SignatureException("Could not recover public key from signature");
    return key;
  }

  public static byte[] signatureToAddress(byte[] messageHash, String signatureBase64)
      throws SignatureException {
    return computeAddress(signatureToKeyBytes(messageHash, signatureBase64));
  }

  public static byte[] signatureToAddress(byte[] messageHash, ECDSASignature sig)
      throws SignatureException {
    return computeAddress(signatureToKeyBytes(messageHash, sig));
  }

  public static ECKey signatureToKey(byte[] messageHash, String signatureBase64)
      throws SignatureException {
    byte[] keyBytes = signatureToKeyBytes(messageHash, signatureBase64);
    return fromPublicOnly(keyBytes);
  }

  public static ECKey signatureToKey(byte[] messageHash, ECDSASignature sig)
      throws SignatureException {
    byte[] keyBytes = signatureToKeyBytes(messageHash, sig);
    return fromPublicOnly(keyBytes);
  }

  public static boolean verify(byte[] data, ECDSASignature signature, byte[] pub) {
    ECDSASigner signer = new ECDSASigner();
    ECPublicKeyParameters params = new ECPublicKeyParameters(CURVE.getCurve()
        .decodePoint(pub), CURVE);
    signer.init(false, params);
    try {
      return signer.verifySignature(data, signature.r, signature.s);
    } catch (NullPointerException npe) {
      return false;
    }
  }

  public static boolean verify(byte[] data, byte[] signature, byte[] pub) {
    return verify(data, ECDSASignature.decodeFromDER(signature), pub);
  }

  public static boolean isPubKeyCanonical(byte[] pubkey) {
    if (pubkey[0] == 0x04) {
      return pubkey.length == 65;
    } else if (pubkey[0] == 0x02 || pubkey[0] == 0x03) {
      return pubkey.length == 33;
    } else {
      return false;
    }
  }

  @Nullable public static byte[] recoverPubBytesFromSignature(int recId, ECDSASignature sig,
      byte[] messageHash) {
    check(recId >= 0, "recId must be positive");
    check(sig.r.signum() >= 0, "r must be positive");
    check(sig.s.signum() >= 0, "s must be positive");
    check(messageHash != null, "messageHash must not be null");
    BigInteger n = CURVE.getN();
    BigInteger i = BigInteger.valueOf((long) recId / 2);
    BigInteger x = sig.r.add(i.multiply(n));
    ECCurve.Fp curve = (ECCurve.Fp) CURVE.getCurve();
    BigInteger prime = curve.getQ();
    if (x.compareTo(prime) >= 0) {
      return null;
    }
    ECPoint R = decompressKey(x, (recId & 1) == 1);
    if (!R.multiply(n)
        .isInfinity()) {
      return null;
    }
    BigInteger e = new BigInteger(1, messageHash);
    BigInteger eInv = BigInteger.ZERO.subtract(e)
        .mod(n);
    BigInteger rInv = sig.r.modInverse(n);
    BigInteger srInv = rInv.multiply(sig.s)
        .mod(n);
    BigInteger eInvrInv = rInv.multiply(eInv)
        .mod(n);
    Fp q = (Fp) ECAlgorithms.sumOfTwoMultiplies(CURVE.getG(), eInvrInv, R, srInv);
    return q.getEncoded(/* compressed */ false);
  }

  @Nullable public static byte[] recoverAddressFromSignature(int recId, ECDSASignature sig,
      byte[] messageHash) {
    byte[] pubBytes = recoverPubBytesFromSignature(recId, sig, messageHash);
    if (pubBytes == null) {
      return null;
    } else {
      return computeAddress(pubBytes);
    }
  }

  @Nullable
  public static ECKey recoverFromSignature(int recId, ECDSASignature sig, byte[] messageHash) {
    byte[] pubBytes = recoverPubBytesFromSignature(recId, sig, messageHash);
    if (pubBytes == null) {
      return null;
    } else {
      return fromPublicOnly(pubBytes);
    }
  }

  private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
    X9IntegerConverter x9 = new X9IntegerConverter();
    byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.getCurve()));
    compEnc[0] = (byte) (yBit ? 0x03 : 0x02);
    return CURVE.getCurve()
        .decodePoint(compEnc);
  }

  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("pub:")
        .append(Hex.toHexString(pub.getEncoded(false)));
    return b.toString();
  }

  private static void check(boolean test, String message) {
    if (!test) throw new IllegalArgumentException(message);
  }

  public boolean isPubKeyOnly() {
    return privKey == null;
  }

  public boolean hasPrivKey() {
    return privKey != null;
  }

  public byte[] getAddress() {
    if (pubKeyHash == null) {
      pubKeyHash = computeAddress(this.pub);
    }
    return pubKeyHash;
  }

  public byte[] getNodeId() {
    if (nodeId == null) {
      nodeId = pubBytesWithoutFormat(this.pub);
    }
    return nodeId;
  }

  public byte[] getPubKey() {
    return pub.getEncoded(/* compressed */ false);
  }

  public ECPoint getPubKeyPoint() {
    return pub;
  }

  public BigInteger getPrivKey() {
    if (privKey == null) {
      throw new MissingPrivateKeyException();
    } else if (privKey instanceof BCECPrivateKey) {
      return ((BCECPrivateKey) privKey).getD();
    } else {
      throw new MissingPrivateKeyException();
    }
  }

  public String toStringWithPrivate() {
    StringBuilder b = new StringBuilder();
    b.append(toString());
    if (privKey != null && privKey instanceof BCECPrivateKey) {
      b.append(" priv:")
          .append(Hex.toHexString(((BCECPrivateKey) privKey).getD()
              .toByteArray()));
    }
    return b.toString();
  }

  public ECDSASignature doSign(byte[] input) {
    if (input.length != 32) {
      throw new IllegalArgumentException(
          "Expected 32 byte input to ECDSA signature, not " + input.length);
    }
    if (privKey == null) throw new MissingPrivateKeyException();
    if (privKey instanceof BCECPrivateKey) {
      ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
      ECPrivateKeyParameters privKeyParams =
          new ECPrivateKeyParameters(((BCECPrivateKey) privKey).getD(), CURVE);
      signer.init(true, privKeyParams);
      BigInteger[] components = signer.generateSignature(input);
      return new ECDSASignature(components[0], components[1]).toCanonicalised();
    } else {
      try {
        Signature ecSig = ECSignatureFactory.getRawInstance(provider);
        ecSig.initSign(privKey);
        ecSig.update(input);
        byte[] derSignature = ecSig.sign();
        return ECDSASignature.decodeFromDER(derSignature)
            .toCanonicalised();
      } catch (SignatureException | InvalidKeyException ex) {
        throw new RuntimeException("ECKey signing error", ex);
      }
    }
  }

  public ECDSASignature sign(byte[] messageHash) {
    ECDSASignature sig = doSign(messageHash);
    int recId = -1;
    byte[] thisKey = this.pub.getEncoded(/* compressed */ false);
    for (int i = 0; i < 4; i++) {
      byte[] k = recoverPubBytesFromSignature(i, sig, messageHash);
      if (k != null && Arrays.equals(k, thisKey)) {
        recId = i;
        break;
      }
    }
    if (recId == -1) {
      throw new RuntimeException(
          "Could not construct a recoverable key. This should never happen.");
    }
    sig.v = (byte) (recId + 27);
    return sig;
  }

  public boolean verify(byte[] data, byte[] signature) {
    return verify(data, signature, getPubKey());
  }

  public boolean verify(byte[] sigHash, ECDSASignature signature) {
    return verify(sigHash, signature, getPubKey());
  }

  public boolean isPubKeyCanonical() {
    return isPubKeyCanonical(pub.getEncoded(/* uncompressed */ false));
  }

  @Nullable public byte[] getPrivKeyBytes() {
    if (privKey == null) {
      return null;
    } else if (privKey instanceof BCECPrivateKey) {
      return bigIntegerToBytes(((BCECPrivateKey) privKey).getD(), 32);
    } else {
      return null;
    }
  }

  public static class ECDSASignature {

    private static final int SIGNATURE_HEX_LENGTH = 130;

    public final BigInteger r, s;
    public byte v;

    public ECDSASignature(BigInteger r, BigInteger s) {
      this.r = r;
      this.s = s;
    }

    private static ECDSASignature fromComponents(byte[] r, byte[] s) {
      return new ECDSASignature(new BigInteger(1, r), new BigInteger(1, s));
    }

    public static ECDSASignature fromComponents(byte[] r, byte[] s, byte v) {
      ECDSASignature signature = fromComponents(r, s);
      signature.v = v;
      return signature;
    }

    public static boolean validateComponents(BigInteger r, BigInteger s, byte v) {

      if (v != 27 && v != 28) return false;

      if (isLessThan(r, BigInteger.ONE)) return false;
      if (isLessThan(s, BigInteger.ONE)) return false;

      if (!isLessThan(r, Constants.getSECP256K1N())) return false;
      return isLessThan(s, Constants.getSECP256K1N());
    }

    public static ECDSASignature decodeFromDER(byte[] bytes) {
      ASN1InputStream decoder = null;
      try {
        decoder = new ASN1InputStream(bytes);
        DLSequence seq = (DLSequence) decoder.readObject();
        if (seq == null) throw new RuntimeException("Reached past end of ASN.1 stream.");
        ASN1Integer r, s;
        try {
          r = (ASN1Integer) seq.getObjectAt(0);
          s = (ASN1Integer) seq.getObjectAt(1);
        } catch (ClassCastException e) {
          throw new IllegalArgumentException(e);
        }
        return new ECDSASignature(r.getPositiveValue(), s.getPositiveValue());
      } catch (IOException e) {
        throw new RuntimeException(e);
      } finally {
        if (decoder != null) {
          try {
            decoder.close();
          } catch (IOException x) {
          }
        }
      }
    }

    public boolean validateComponents() {
      return validateComponents(r, s, v);
    }

    public ECDSASignature toCanonicalised() {
      if (s.compareTo(HALF_CURVE_ORDER) > 0) {
        return new ECDSASignature(r, CURVE.getN()
            .subtract(s));
      } else {
        return this;
      }
    }

    public String toBase64() {
      byte[] sigData = new byte[65];
      sigData[0] = v;
      System.arraycopy(bigIntegerToBytes(this.r, 32), 0, sigData, 1, 32);
      System.arraycopy(bigIntegerToBytes(this.s, 32), 0, sigData, 33, 32);
      return new String(Base64.encode(sigData), StandardCharsets.UTF_8);
    }

    public byte[] toByteArray() {
      byte fixedV = this.v >= 27 ? (byte) (this.v - 27) : this.v;

      return ByteUtil.merge(bigIntegerToBytes(this.r, 32), bigIntegerToBytes(this.s, 32),
          new byte[] { fixedV });
    }

    public String toHex() {
      return normalize(Hex.toHexString(toByteArray()));
    }

    private String normalize(String s) {
      StringBuilder builder = new StringBuilder(s);

      for (int i = 0; i < (SIGNATURE_HEX_LENGTH - s.length()); i++) {
        builder.insert(0, '0');
      }
      return builder.toString();
    }

    @Override public int hashCode() {
      int result = r.hashCode();
      result = 31 * result + s.hashCode();
      return result;
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      ECDSASignature signature = (ECDSASignature) o;

      if (!r.equals(signature.r)) return false;
      return s.equals(signature.s);
    }
  }

  @SuppressWarnings("serial") public static class MissingPrivateKeyException
      extends RuntimeException {
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || !(o instanceof ECKey)) return false;

    ECKey ecKey = (ECKey) o;

    if (privKey != null && !privKey.equals(ecKey.privKey)) return false;
    return !(pub != null && !pub.equals(ecKey.pub));
  }

  @Override public int hashCode() {
    return Arrays.hashCode(getPubKey());
  }
}
