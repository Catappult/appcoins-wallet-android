package com.asfoundation.wallet.service;

import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.PendingTransaction;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.EthereumNetworkRepositoryType;
import com.asfoundation.wallet.repository.PendingTransactionService;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import java.util.ArrayList;
import java.util.List;
import retrofit2.HttpException;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by trinkes on 3/16/18.
 */

public class AirDropService {
  public static final String BASE_URL = "https://api.appstorefoundation.org/";
  private final Api api;
  private final PendingTransactionService pendingTransactionService;
  private final EthereumNetworkRepositoryType repository;
  private final BehaviorSubject<Airdrop> airdropResponse;
  private final AirdropChainIdMapper airdropChainIdMapper;
  private final Gson gson;

  public AirDropService(PendingTransactionService pendingTransactionService,
      EthereumNetworkRepositoryType repository, BehaviorSubject<Airdrop> airdropResponse, Api api,
      AirdropChainIdMapper airdropChainIdMapper, Gson gson) {
    this.pendingTransactionService = pendingTransactionService;
    this.repository = repository;
    this.airdropResponse = airdropResponse;
    this.api = api;
    this.airdropChainIdMapper = airdropChainIdMapper;
    this.gson = gson;
  }

  public void request(Wallet wallet) {
    airdropChainIdMapper.getAirdropChainId()
        .observeOn(Schedulers.io())
        .flatMap(chainId -> api.requestCoins(wallet.address, chainId))
        .doOnSubscribe(__ -> airdropResponse.onNext(new Airdrop(Airdrop.AirdropStatus.PENDING)))
        .doOnSuccess(airDropResponse -> {
          for (NetworkInfo networkInfo : repository.getAvailableNetworkList()) {
            if (airDropResponse.getChainId() == networkInfo.chainId) {
              repository.setDefaultNetworkInfo(networkInfo);
            }
          }
        })
        .doOnSuccess(airDropResponse -> publish(Airdrop.AirdropStatus.PENDING))
        .flatMapCompletable(airDropResponse -> {
          List<Completable> list = new ArrayList<>();
          list.add(waitTransactionComplete(airDropResponse.getAppcoinsTransaction()));
          list.add(waitTransactionComplete(airDropResponse.getEthTransaction()));
          return Completable.merge(list)
              .andThen(Completable.fromAction(() -> airdropResponse.onNext(
                  new Airdrop(Airdrop.AirdropStatus.SUCCESS, airDropResponse.getDescription()))));
        })
        .subscribe(() -> {
        }, throwable -> publish(throwable));
  }

  private void publish(Throwable throwable) {
    throwable.printStackTrace();
    if (throwable instanceof HttpException) {
      airdropResponse.onNext(new Airdrop(Airdrop.AirdropStatus.API_ERROR, gson.fromJson(
          ((HttpException) throwable).response()
              .errorBody()
              .charStream(), AirdropErrorResponse.class)
          .getDescription()));
    } else {
      publish(Airdrop.AirdropStatus.ERROR);
    }
  }

  private void publish(Airdrop.AirdropStatus status) {
    airdropResponse.onNext(new Airdrop(status));
  }

  private Completable waitTransactionComplete(String transactionHash) {
    return pendingTransactionService.checkTransactionState(transactionHash)
        .onErrorResumeNext(Observable.just(new PendingTransaction(null, true)))
        .ignoreElements();
  }

  public Observable<Airdrop> getStatus() {
    return airdropResponse.filter(
        airdropStatus -> airdropStatus.getStatus() != Airdrop.AirdropStatus.EMPTY);
  }

  public void resetStatus() {
    airdropResponse.onNext(new Airdrop(Airdrop.AirdropStatus.EMPTY));
  }

  public interface Api {

    @GET("airdrop/{address}/funds") Single<AirDropResponse> requestCoins(
        @Path("address") String address, @Query("chain_id") int chainId);
  }

  private static class AirdropErrorResponse {
    String description;

    public AirdropErrorResponse() {
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }
  }

  private static class AirDropResponse {
    @SerializedName("txid_appc") private String appcoinsTransaction;
    @SerializedName("txid_eth") private String ethTransaction;
    @SerializedName("chain_id") private int chainId;
    @SerializedName("description") private String description;

    public AirDropResponse() {
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
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
