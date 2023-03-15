package com.asfoundation.wallet.repository;

import com.appcoins.wallet.networkbase.annotations.BlockchainHttpClient;
import com.asfoundation.wallet.entity.NetworkInfo;
import javax.inject.Inject;
import javax.inject.Named;
import okhttp3.OkHttpClient;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

/**
 * Created by trinkes on 26/02/2018.
 */

public class Web3jProvider {

  private final OkHttpClient httpClient;
  private final NetworkInfo networkInfo;
  private Web3j web3j;

  public @Inject Web3jProvider(@BlockchainHttpClient OkHttpClient client, NetworkInfo networkInfo) {
    httpClient = client;
    this.networkInfo = networkInfo;
    buildWeb3jClient(networkInfo);
  }

  private void buildWeb3jClient(NetworkInfo defaultNetwork) {
    web3j = Web3j.build(new HttpService(defaultNetwork.rpcServerUrl, httpClient, false));
  }

  public Web3j getDefault() {
    return web3j;
  }

  public Web3j get() {
    return Web3j.build(new HttpService(networkInfo.rpcServerUrl, httpClient, false));
  }
}
