package com.asf.wallet.service;

import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.EthereumNetworkRepositoryType;
import com.asfoundation.wallet.repository.PendingTransactionService;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;
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
  private final BehaviorSubject<AirdropStatus> airdropResponse;

  public AirDropService(OkHttpClient httpClient, Gson gson,
      PendingTransactionService pendingTransactionService, EthereumNetworkRepositoryType repository,
      BehaviorSubject<AirdropStatus> airdropResponse) {
    this.pendingTransactionService = pendingTransactionService;
    this.repository = repository;
    this.airdropResponse = airdropResponse;
    api = new Retrofit.Builder().baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(Api.class);
  }

  public void request(Wallet wallet) {
    api.requestCoins(wallet.address)
        .doOnSubscribe(__ -> airdropResponse.onNext(AirdropStatus.PENDING))
        .doOnSuccess(airDropResponse -> {
          for (NetworkInfo networkInfo : repository.getAvailableNetworkList()) {
            if (airDropResponse.getChainId() == networkInfo.chainId) {
              repository.setDefaultNetworkInfo(networkInfo);
            }
          }
        })
        .doOnSuccess(airDropResponse -> publish(AirdropStatus.PENDING))
        .flatMapCompletable(airDropResponse -> {
          List<Completable> list = new ArrayList<>();
          list.add(waitTransactionComplete(airDropResponse.getAppcoinsTransaction()));
          list.add(waitTransactionComplete(airDropResponse.getEthTransaction()));
          return Completable.merge(list);
        })
        .doOnTerminate(() -> publish(AirdropStatus.DONE))
        .subscribe(() -> {
        }, throwable -> publish(throwable));
  }

  private void publish(Throwable throwable) {
    throwable.printStackTrace();
    publish(AirdropStatus.ERROR);
  }

  private void publish(AirdropStatus status) {
    airdropResponse.onNext(status);
  }

  private Completable waitTransactionComplete(String transactionHash) {
    return pendingTransactionService.checkTransactionState(transactionHash)
        .ignoreElements();
  }

  public Observable<AirdropStatus> getStatus() {
    return airdropResponse.filter(airdropStatus -> airdropStatus != AirdropStatus.EMPTY);
  }

  public void resetStatus() {
    airdropResponse.onNext(AirdropStatus.EMPTY);
  }

  public enum AirdropStatus {
    PENDING, ERROR, ENDED, DONE, EMPTY
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
