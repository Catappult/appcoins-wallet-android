package com.asfoundation.wallet.interact;

import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.TokenInfo;
import io.reactivex.Single;
import org.jetbrains.annotations.NotNull;

/**
 * Created by trinkes on 07/02/2018.
 */

public class BuildConfigDefaultTokenProvider implements DefaultTokenProvider {
  private final FindDefaultWalletInteract findDefaultWalletInteract;
  private final NetworkInfo defaultNetwork;

  public BuildConfigDefaultTokenProvider(FindDefaultWalletInteract findDefaultWalletInteract,
      NetworkInfo defaultNetwork) {
    this.findDefaultWalletInteract = findDefaultWalletInteract;
    this.defaultNetwork = defaultNetwork;
  }

  @NotNull @Override public Single<TokenInfo> getDefaultToken() {
    return findDefaultWalletInteract.find()
        .map(wallet -> getDefaultToken(defaultNetwork));
  }

  @NotNull private TokenInfo getDefaultToken(@NotNull NetworkInfo networkInfo) {
    switch (networkInfo.chainId) {
      // MAIN
      case 1:
      default:
        return new TokenInfo(BuildConfig.MAIN_NETWORK_DEFAULT_TOKEN_ADDRESS,
            BuildConfig.MAIN_NETWORK_DEFAULT_TOKEN_NAME,
            BuildConfig.MAIN_NETWORK_DEFAULT_TOKEN_SYMBOL.toLowerCase(),
            BuildConfig.MAIN_NETWORK_DEFAULT_TOKEN_DECIMALS);
      //  ROPSTEN
      case 3:
        return new TokenInfo(BuildConfig.ROPSTEN_DEFAULT_TOKEN_ADDRESS,
            BuildConfig.ROPSTEN_DEFAULT_TOKEN_NAME,
            BuildConfig.ROPSTEN_DEFAULT_TOKEN_SYMBOL.toLowerCase(),
            BuildConfig.ROPSTEN_DEFAULT_TOKEN_DECIMALS);
    }
  }
}
