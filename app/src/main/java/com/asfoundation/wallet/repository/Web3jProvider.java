package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.NetworkInfo;
import okhttp3.OkHttpClient;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;

/**
 * Created by trinkes on 26/02/2018.
 */

public class Web3jProvider {

  private final OkHttpClient httpClient;
  private final NetworkInfo networkInfo;
  private Web3j web3j;

  public Web3jProvider(OkHttpClient client, NetworkInfo networkInfo) {
    httpClient = client;
    this.networkInfo = networkInfo;
    buildWeb3jClient(networkInfo);
  }

  private void buildWeb3jClient(NetworkInfo defaultNetwork) {
    web3j = Web3jFactory.build(new HttpService(defaultNetwork.rpcServerUrl, httpClient, false));
  }

  public Web3j getDefault() {
    return web3j;
  }

  public Web3j get() {
    return Web3jFactory.build(new HttpService(networkInfo.rpcServerUrl, httpClient, false));
  }
}
