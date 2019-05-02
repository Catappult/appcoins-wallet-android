package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.NetworkInfo;

public class EthereumNetworkRepository implements EthereumNetworkRepositoryType {

  private NetworkInfo defaultNetwork;

  public EthereumNetworkRepository(PreferenceRepositoryType preferences,
      NetworkInfo defaultNetworkInfo) {
    defaultNetwork = defaultNetworkInfo;
    preferences.setDefaultNetwork(defaultNetworkInfo.name);
  }

  @Override public NetworkInfo getDefaultNetwork() {
    return defaultNetwork;
  }
}
