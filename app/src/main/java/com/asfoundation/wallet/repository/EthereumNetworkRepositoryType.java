package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.NetworkInfo;

public interface EthereumNetworkRepositoryType {

  NetworkInfo getDefaultNetwork();
}
