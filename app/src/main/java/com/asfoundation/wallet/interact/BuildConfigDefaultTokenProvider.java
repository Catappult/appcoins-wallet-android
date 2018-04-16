package com.asfoundation.wallet.interact;

import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.TokenInfo;
import com.asfoundation.wallet.entity.Wallet;
import io.reactivex.Single;

import static com.asfoundation.wallet.C.CLASSIC_NETWORK_NAME;
import static com.asfoundation.wallet.C.ETC_SYMBOL;
import static com.asfoundation.wallet.C.ETHER_DECIMALS;
import static com.asfoundation.wallet.C.ETH_SYMBOL;
import static com.asfoundation.wallet.C.KOVAN_NETWORK_NAME;
import static com.asfoundation.wallet.C.POA_NETWORK_NAME;
import static com.asfoundation.wallet.C.POA_SYMBOL;
import static com.asfoundation.wallet.C.SOKOL_NETWORK_NAME;

/**
 * Created by trinkes on 07/02/2018.
 */

public class BuildConfigDefaultTokenProvider implements DefaultTokenProvider {
  private final FindDefaultNetworkInteract defaultNetworkInteract;
  private final FindDefaultWalletInteract findDefaultWalletInteract;

  public BuildConfigDefaultTokenProvider(FindDefaultNetworkInteract defaultNetworkInteract,
      FindDefaultWalletInteract findDefaultWalletInteract) {
    this.defaultNetworkInteract = defaultNetworkInteract;
    this.findDefaultWalletInteract = findDefaultWalletInteract;
  }

  @Override public Single<TokenInfo> getDefaultToken() {
    return defaultNetworkInteract.find()
        .flatMap(networkInfo -> findDefaultWalletInteract.find()
            .map(wallet -> getDefaultToken(networkInfo, wallet)));
  }

  @Override public Single<String> getAdsAddress() {
    return defaultNetworkInteract.find()
        .map(this::getDefaultAdsAddress);
  }

  private String getDefaultAdsAddress(NetworkInfo networkInfo) {
    switch (networkInfo.chainId) {
      case 3:
        return BuildConfig.ROPSTEN_NETWORK_ASF_ADS_CONTRACT_ADDRESS;
      default:
        return BuildConfig.MAIN_NETWORK_ASF_ADS_CONTRACT_ADDRESS;
    }
  }

  private TokenInfo getDefaultToken(NetworkInfo networkInfo, Wallet wallet) {
    switch (networkInfo.chainId) {
      // MAIN
      case 1:
      default:
        return new TokenInfo(BuildConfig.MAIN_NETWORK_DEFAULT_TOKEN_ADDRESS,
            BuildConfig.MAIN_NETWORK_DEFAULT_TOKEN_NAME,
            BuildConfig.MAIN_NETWORK_DEFAULT_TOKEN_SYMBOL,
            BuildConfig.MAIN_NETWORK_DEFAULT_TOKEN_DECIMALS, true, false);
      //  CLASSIC
      case 61:
        return new TokenInfo(wallet.address, CLASSIC_NETWORK_NAME, ETC_SYMBOL, ETHER_DECIMALS, true,
            false);
      //  POA
      case 99:
        return new TokenInfo(wallet.address, POA_NETWORK_NAME, POA_SYMBOL, ETHER_DECIMALS, true,
            false);
      //  KOVAN
      case 42:
        return new TokenInfo(wallet.address, KOVAN_NETWORK_NAME, ETH_SYMBOL, ETHER_DECIMALS, true,
            false);
      //  ROPSTEN
      case 3:
        return new TokenInfo(BuildConfig.ROPSTEN_DEFAULT_TOKEN_ADDRESS,
            BuildConfig.ROPSTEN_DEFAULT_TOKEN_NAME, BuildConfig.ROPSTEN_DEFAULT_TOKEN_SYMBOL,
            BuildConfig.ROPSTEN_DEFAULT_TOKEN_DECIMALS, true, false);
      //  SOKOL
      case 77:
        return new TokenInfo(wallet.address, SOKOL_NETWORK_NAME, ETH_SYMBOL, ETHER_DECIMALS, true,
            false);
    }
  }
}
