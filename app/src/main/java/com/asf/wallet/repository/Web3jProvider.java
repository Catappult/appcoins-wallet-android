package com.asf.wallet.repository;

import com.asf.wallet.entity.NetworkInfo;
import okhttp3.OkHttpClient;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;

/**
 * Created by trinkes on 26/02/2018.
 */

public class Web3jProvider {

  private final OkHttpClient httpClient;
  private Web3j web3j;

  public Web3jProvider(EthereumNetworkRepositoryType ethereumNetworkRepository,
      OkHttpClient client) {
    httpClient = client;
    ethereumNetworkRepository.addOnChangeDefaultNetwork(this::buildWeb3jClient);
    buildWeb3jClient(ethereumNetworkRepository.getDefaultNetwork());
  }

  private void buildWeb3jClient(NetworkInfo defaultNetwork) {
    web3j = Web3jFactory.build(new HttpService(defaultNetwork.rpcServerUrl, httpClient, false));
  }

  public Web3j get() {
    return web3j;
  }
}
