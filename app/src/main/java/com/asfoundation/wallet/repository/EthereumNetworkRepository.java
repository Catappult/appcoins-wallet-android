package com.asfoundation.wallet.repository;

import android.text.TextUtils;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.Ticker;
import com.asfoundation.wallet.service.TickerService;
import io.reactivex.Single;
import java.util.HashSet;
import java.util.Set;

import static com.asfoundation.wallet.C.CLASSIC_NETWORK_NAME;
import static com.asfoundation.wallet.C.ETC_SYMBOL;
import static com.asfoundation.wallet.C.ETHEREUM_NETWORK_NAME;
import static com.asfoundation.wallet.C.ETH_SYMBOL;
import static com.asfoundation.wallet.C.KOVAN_NETWORK_NAME;
import static com.asfoundation.wallet.C.POA_NETWORK_NAME;
import static com.asfoundation.wallet.C.POA_SYMBOL;
import static com.asfoundation.wallet.C.ROPSTEN_NETWORK_NAME;
import static com.asfoundation.wallet.C.SOKOL_NETWORK_NAME;

public class EthereumNetworkRepository implements EthereumNetworkRepositoryType {

  private final NetworkInfo[] NETWORKS = new NetworkInfo[] {
      new NetworkInfo(ETHEREUM_NETWORK_NAME, ETH_SYMBOL,
          "https://mainnet.infura.io/llyrtzQ3YhkdESt2Fzrk", "https://api.trustwalletapp.com/",
          "https://etherscan.io/tx/", 1, true),
      new NetworkInfo(CLASSIC_NETWORK_NAME, ETC_SYMBOL, "https://mewapi.epool.io/",
          "https://classic.trustwalletapp.com", "https://gastracker.io/tx/", 61, true),
      new NetworkInfo(POA_NETWORK_NAME, POA_SYMBOL, "https://core.poa.network/",
          "https://poa.trustwalletapp.com", "https://poaexplorer.com/txid/search/", 99, false),
      new NetworkInfo(KOVAN_NETWORK_NAME, ETH_SYMBOL,
          "https://kovan.infura.io/llyrtzQ3YhkdESt2Fzrk", "https://kovan.trustwalletapp.com/",
          "https://kovan.etherscan.io/tx/", 42, false),
      new NetworkInfo(ROPSTEN_NETWORK_NAME, ETH_SYMBOL,
          "https://ropsten.infura.io/llyrtzQ3YhkdESt2Fzrk", "https://ropsten.trustwalletapp.com/",
          "https://ropsten.etherscan.io/tx/", 3, false),
      new NetworkInfo(SOKOL_NETWORK_NAME, POA_SYMBOL, "https://sokol.poa.network",
          "https://trust-sokol.herokuapp.com/", "https://sokol-explorer.poa.network/account/", 77,
          false),
  };

  private final PreferenceRepositoryType preferences;
  private final TickerService tickerService;
  private final Set<OnNetworkChangeListener> onNetworkChangedListeners = new HashSet<>();
  private NetworkInfo defaultNetwork;

  public EthereumNetworkRepository(PreferenceRepositoryType preferenceRepository,
      TickerService tickerService) {
    this.preferences = preferenceRepository;
    this.tickerService = tickerService;
    defaultNetwork = getByName(preferences.getDefaultNetwork());
    if (defaultNetwork == null) {
      defaultNetwork = NETWORKS[0];
    }
  }

  private NetworkInfo getByName(String name) {
    if (!TextUtils.isEmpty(name)) {
      for (NetworkInfo NETWORK : NETWORKS) {
        if (name.equals(NETWORK.name)) {
          return NETWORK;
        }
      }
    }
    return null;
  }

  @Override public NetworkInfo getDefaultNetwork() {
    return defaultNetwork;
  }

  @Override public void setDefaultNetworkInfo(NetworkInfo networkInfo) {
    defaultNetwork = networkInfo;
    preferences.setDefaultNetwork(defaultNetwork.name);

    for (OnNetworkChangeListener listener : onNetworkChangedListeners) {
      listener.onNetworkChanged(networkInfo);
    }
  }

  @Override public NetworkInfo[] getAvailableNetworkList() {
    return NETWORKS;
  }

  @Override public void addOnChangeDefaultNetwork(OnNetworkChangeListener onNetworkChanged) {
    onNetworkChangedListeners.add(onNetworkChanged);
  }

  @Override public Single<Ticker> getTicker() {
    return Single.fromObservable(tickerService.fetchTickerPrice(getDefaultNetwork().symbol));
  }

  /**
   * execute a single on a specific network and after terminate, restore the network to the
   * previous one
   * use it only when doing fast operations!
   *
   * @param chainId - identifies the network where the single should run
   * @param single - single to run
   */
  @Override public <T> Single<T> executeOnNetworkAndRestore(int chainId, Single<T> single) {
    return Single.just(getDefaultNetwork())
        .doOnSuccess(__ -> setNetwork(chainId))
        .flatMap(defaultNetworkInfo -> single.doAfterTerminate(
            () -> setDefaultNetworkInfo(defaultNetworkInfo)));
  }

  private void setNetwork(int chainId) {
    for (NetworkInfo networkInfo : getAvailableNetworkList()) {
      if (chainId == networkInfo.chainId) {
        setDefaultNetworkInfo(networkInfo);
      }
    }
  }
}
