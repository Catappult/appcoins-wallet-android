package com.asfoundation.wallet.service;

import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.EthereumNetworkRepositoryType;
import com.asfoundation.wallet.repository.PendingTransactionService;
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
  private final BehaviorSubject<AirdropStatus> airdropResponse;
  private final AirdropChainIdMapper airdropChainIdMapper;

  public AirDropService(PendingTransactionService pendingTransactionService,
      EthereumNetworkRepositoryType repository, BehaviorSubject<AirdropStatus> airdropResponse,
      Api api, AirdropChainIdMapper airdropChainIdMapper) {
    this.pendingTransactionService = pendingTransactionService;
    this.repository = repository;
    this.airdropResponse = airdropResponse;
    this.api = api;
    this.airdropChainIdMapper = airdropChainIdMapper;
  }

  public void request(Wallet wallet) {
    airdropChainIdMapper.getAirdropChainId()
        .observeOn(Schedulers.io())
        .flatMap(chainId -> api.requestCoins(wallet.address, chainId))
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
        .andThen(Completable.fromAction(() -> publish(AirdropStatus.SUCCESS)))
        .subscribe(() -> {
        }, throwable -> publish(throwable));
  }

  private void publish(Throwable throwable) {
    throwable.printStackTrace();
    if (throwable instanceof HttpException && ((HttpException) throwable).code() == 404) {
      publish(AirdropStatus.ENDED);
    } else {
      publish(AirdropStatus.ERROR);
    }
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
    PENDING, ERROR, ENDED, SUCCESS, EMPTY
  }

  public interface Api {

    @GET("airdrop/{address}/funds") Single<AirDropResponse> requestCoins(
        @Path("address") String address, @Query("chain_id") int chainId);
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
