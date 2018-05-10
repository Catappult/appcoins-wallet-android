package com.asfoundation.wallet.service;

import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.repository.EthereumNetworkRepositoryType;
import com.asfoundation.wallet.repository.PendingTransactionService;
import com.asfoundation.wallet.repository.TransactionNotFoundException;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AirdropInteractor {

  private final PendingTransactionService pendingTransactionService;
  private final EthereumNetworkRepositoryType repository;
  private final BehaviorSubject<Airdrop> airdropResponse;
  private final AirdropChainIdMapper airdropChainIdMapper;
  private final AirdropService airdropService;
  private Disposable disposable;

  public AirdropInteractor(PendingTransactionService pendingTransactionService,
      EthereumNetworkRepositoryType repository, BehaviorSubject<Airdrop> airdropResponse,
      AirdropChainIdMapper airdropChainIdMapper, AirdropService airdropService) {
    this.pendingTransactionService = pendingTransactionService;
    this.repository = repository;
    this.airdropResponse = airdropResponse;
    this.airdropChainIdMapper = airdropChainIdMapper;
    this.airdropService = airdropService;
  }

  public void request(String walletAddress) {
    disposable = airdropChainIdMapper.getAirdropChainId()
        .doOnSubscribe(__ -> airdropResponse.onNext(new Airdrop(Airdrop.AirdropStatus.PENDING)))
        .flatMap(chainId -> airdropService.request(walletAddress, chainId))
        .flatMapCompletable(airDropResponse -> {
          switch (airDropResponse.getStatus()) {
            default:
            case OK:
              return Completable.fromAction(() -> setNetwork(airDropResponse))
                  .andThen(waitForTransactions(airDropResponse));
            case FAIL:
              return Completable.fromAction(() -> publishApiError(airDropResponse));
          }
        })
        .subscribe(() -> {
        }, throwable -> publish(Airdrop.AirdropStatus.ERROR));
  }

  public void stop() {
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
    }
  }

  private CompletableSource waitForTransactions(AirdropService.AirDropResponse airDropResponse) {
    List<Completable> list = new ArrayList<>();
    list.add(waitTransactionComplete(airDropResponse.getAppcoinsTransaction()));
    list.add(waitTransactionComplete(airDropResponse.getEthTransaction()));
    return Completable.merge(list)
        .andThen(Completable.fromAction(() -> airdropResponse.onNext(
            new Airdrop(Airdrop.AirdropStatus.SUCCESS, airDropResponse.getDescription()))));
  }

  private void publishApiError(AirdropService.AirDropResponse airDropResponse) {
    publish(Airdrop.AirdropStatus.API_ERROR, airDropResponse.getDescription());
  }

  public void setNetwork(AirdropService.AirDropResponse airDropResponse) {
    for (NetworkInfo networkInfo : repository.getAvailableNetworkList()) {
      if (airDropResponse.getChainId() == networkInfo.chainId) {
        repository.setDefaultNetworkInfo(networkInfo);
      }
    }
  }

  private void publish(Airdrop.AirdropStatus status, String description) {
    airdropResponse.onNext(new Airdrop(status, description));
  }

  private void publish(Airdrop.AirdropStatus status) {
    airdropResponse.onNext(new Airdrop(status));
  }

  private Completable waitTransactionComplete(String transactionHash) {
    return pendingTransactionService.checkTransactionState(transactionHash)
        .retryWhen(throwableObservable -> throwableObservable.flatMap(throwable -> {
          if (throwable instanceof TransactionNotFoundException) {
            return Observable.timer(5, TimeUnit.SECONDS);
          }
          return Observable.error(throwable);
        }))
        .ignoreElements();
  }

  public Observable<Airdrop> getStatus() {
    return airdropResponse.filter(
        airdropStatus -> airdropStatus.getStatus() != Airdrop.AirdropStatus.EMPTY);
  }

  public void resetStatus() {
    airdropResponse.onNext(new Airdrop(Airdrop.AirdropStatus.EMPTY));
  }
}
