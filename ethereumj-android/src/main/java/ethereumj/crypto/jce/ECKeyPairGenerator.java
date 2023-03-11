package ethereumj.crypto.jce;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;

public final class ECKeyPairGenerator {

  public static final String ALGORITHM = "EC";
  public static final String CURVE_NAME = "secp256k1";

  private static final String algorithmAssertionMsg = "Assumed JRE supports EC key pair generation";

  private static final String keySpecAssertionMsg = "Assumed correct key spec statically";

  private static final String providerAssertionMsg = "Assumed provider was available";

  private static final ECGenParameterSpec SECP256K1_CURVE = new ECGenParameterSpec(CURVE_NAME);

  private ECKeyPairGenerator() {
  }

  public static KeyPair generateKeyPair() {
    return Holder.INSTANCE.generateKeyPair();
  }

  public static KeyPairGenerator getInstance(String provider, SecureRandom random)
      throws NoSuchProviderException {
    try {
      KeyPairGenerator gen = KeyPairGenerator.getInstance(ALGORITHM, provider);
      gen.initialize(SECP256K1_CURVE, random);
      return gen;
    } catch (NoSuchAlgorithmException ex) {
      throw new AssertionError(algorithmAssertionMsg, ex);
    } catch (InvalidAlgorithmParameterException ex) {
      throw new AssertionError(keySpecAssertionMsg, ex);
    }
  }

  public static KeyPairGenerator getInstance(Provider provider, SecureRandom random) {
    try {
      KeyPairGenerator gen = KeyPairGenerator.getInstance(ALGORITHM, provider);
      gen.initialize(SECP256K1_CURVE, random);
      return gen;
    } catch (NoSuchAlgorithmException ex) {
      throw new AssertionError(algorithmAssertionMsg, ex);
    } catch (InvalidAlgorithmParameterException ex) {
      throw new AssertionError(keySpecAssertionMsg, ex);
    }
  }

  private static class Holder {
    private static final KeyPairGenerator INSTANCE;

    static {
      try {
        INSTANCE = KeyPairGenerator.getInstance(ALGORITHM, "AndroidKeyStore");
        INSTANCE.initialize(SECP256K1_CURVE);
      } catch (NoSuchProviderException ex) {
        throw new AssertionError(providerAssertionMsg, ex);
      } catch (NoSuchAlgorithmException ex) {
        throw new AssertionError(algorithmAssertionMsg, ex);
      } catch (InvalidAlgorithmParameterException ex) {
        throw new AssertionError(keySpecAssertionMsg, ex);
      }
    }
  }
}
