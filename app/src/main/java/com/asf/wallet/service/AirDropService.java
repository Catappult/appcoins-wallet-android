package com.asf.wallet.service;

import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.EthereumNetworkRepositoryType;
import com.asfoundation.wallet.repository.PendingTransactionService;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import io.reactivex.Completable;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by trinkes on 3/16/18.
 */

public class AirDropService {
  public static final String BASE_URL = "https://api.appstorefoundation.org/";
  private final Api api;
  private final PendingTransactionService pendingTransactionService;
  private final EthereumNetworkRepositoryType repository;

  public AirDropService(OkHttpClient httpClient, Gson gson,
      PendingTransactionService pendingTransactionService,
      EthereumNetworkRepositoryType repository) {
    this.pendingTransactionService = pendingTransactionService;
    this.repository = repository;
    api = new Retrofit.Builder().baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(Api.class);
  }

  public Completable request(Wallet wallet) {
    return api.requestCoins(wallet.address)
        .doOnSuccess(airDropResponse -> {
          for (NetworkInfo networkInfo : repository.getAvailableNetworkList()) {
            if (airDropResponse.getChainId() == networkInfo.chainId) {
              repository.setDefaultNetworkInfo(networkInfo);
            }
          }
        })
        .flatMapCompletable(airDropResponse -> {
          List<Completable> list = new ArrayList<>();
          list.add(waitTransactionComplete(airDropResponse.getAppcoinsTransaction()));
          list.add(waitTransactionComplete(airDropResponse.getEthTransaction()));
          return Completable.merge(list);
        });
  }

  private Completable waitTransactionComplete(String transactionHash) {
    return pendingTransactionService.checkTransactionState(transactionHash)
        .ignoreElements();
  }

  public interface Api {

    @GET("airdrop/{address}/funds") Single<AirDropResponse> requestCoins(
        @Path("address") String address);
  }

  private static class AirDropResponse {
    @SerializedName("txid_appc") private String appcoinsTransaction;
    @SerializedName("txid_eth") private String ethTransaction;
    @SerializedName("chain_id") private int chainId;

    public AirDropResponse() {
    }

    public int getChainId() {
      return chainId;
    }

    public void setChainId(int chainId) {
      this.chainId = chainId;
    }

    public String getEthTransaction() {
      return ethTransaction;
    }

    public void setEthTransaction(String ethTransaction) {
      this.ethTransaction = ethTransaction;
    }

    public String getAppcoinsTransaction() {
      return appcoinsTransaction;
    }

    public void setAppcoinsTransaction(String appcoinsTransaction) {
      this.appcoinsTransaction = appcoinsTransaction;
    }
  }
}
