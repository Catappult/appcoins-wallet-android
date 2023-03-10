package ethereumj.crypto.jce;

import java.security.Provider;
import org.spongycastle.jce.provider.BouncyCastleProvider;

public final class SpongyCastleProvider {

  public static Provider getInstance() {
    return Holder.INSTANCE;
  }

  private static class Holder {
    private static final Provider INSTANCE = new BouncyCastleProvider();
  }
}
