package ethereumj.crypto.jce;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Signature;

public final class ECSignatureFactory {

  public static final String RAW_ALGORITHM = "NONEwithECDSA";

  private static final String rawAlgorithmAssertionMsg =
      "Assumed the JRE supports NONEwithECDSA signatures";

  public static Signature getRawInstance(Provider provider) {
    try {
      return Signature.getInstance(RAW_ALGORITHM, provider);
    } catch (NoSuchAlgorithmException ex) {
      throw new AssertionError(rawAlgorithmAssertionMsg, ex);
    }
  }
}
