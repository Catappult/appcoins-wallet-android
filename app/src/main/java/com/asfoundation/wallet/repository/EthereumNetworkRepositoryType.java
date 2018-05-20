package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.Ticker;
import io.reactivex.Single;

public interface EthereumNetworkRepositoryType {

  NetworkInfo getDefaultNetwork();

  void setDefaultNetworkInfo(NetworkInfo networkInfo);

  NetworkInfo[] getAvailableNetworkList();

  void addOnChangeDefaultNetwork(OnNetworkChangeListener onNetworkChanged);

  Single<Ticker> getTicker();

  <T> Single<T> executeOnNetworkAndRestore(int chainId, Single<T> single);

  void setDefaultNetworkInfo(int chainId);

  NetworkInfo getNetwork(int chainId);
}
