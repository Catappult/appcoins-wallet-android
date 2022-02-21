package ethereumj.crypto;

import org.spongycastle.crypto.KeyEncoder;
import org.spongycastle.crypto.params.AsymmetricKeyParameter;
import org.spongycastle.crypto.params.ECPublicKeyParameters;

public class ECIESPublicKeyEncoder implements KeyEncoder {
  @Override public byte[] getEncoded(AsymmetricKeyParameter asymmetricKeyParameter) {
    return ((ECPublicKeyParameters) asymmetricKeyParameter).getQ()
        .getEncoded(false);
  }
}
