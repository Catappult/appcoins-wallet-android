package com.asf.wallet.repository;

import com.asf.wallet.entity.NetworkInfo;
import com.asf.wallet.entity.Ticker;
import io.reactivex.Single;

public interface EthereumNetworkRepositoryType {

  NetworkInfo getDefaultNetwork();

  void setDefaultNetworkInfo(NetworkInfo networkInfo);

  NetworkInfo[] getAvailableNetworkList();

  void addOnChangeDefaultNetwork(OnNetworkChangeListener onNetworkChanged);

  Single<Ticker> getTicker();
}
