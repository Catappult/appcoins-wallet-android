package com.asfoundation.wallet;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import java.util.ArrayList;
import java.util.List;

public class Airdrop {

  private final TransactionService transactionService;
  private final BehaviorSubject<AirdropData> airdropResponse;
  private final AirdropService airdropService;
  private Disposable disposable;

  public Airdrop(TransactionService transactionService,
      BehaviorSubject<AirdropData> airdropResponse, AirdropService airdropService) {
    this.transactionService = transactionService;
    this.airdropResponse = airdropResponse;
    this.airdropService = airdropService;
  }

  public void request(String walletAddress, int chainId) {
    disposable = airdropService.request(walletAddress, chainId)
        .doOnSubscribe(
            __ -> airdropResponse.onNext(new AirdropData(AirdropData.AirdropStatus.PENDING)))
        .flatMapCompletable(airDropResponse -> {
          switch (airDropResponse.getStatus()) {
            default:
            case OK:
              return waitForTransactions(airDropResponse);
            case FAIL:
              return Completable.fromAction(() -> publishApiError(airDropResponse));
          }
        })
        .subscribe(() -> {
        }, throwable -> publish(AirdropData.AirdropStatus.ERROR));
  }

  public void stop() {
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
    }
  }

  private Completable waitForTransactions(AirdropService.AirDropResponse airDropResponse) {
    List<Completable> list = new ArrayList<>();
    list.add(
        transactionService.waitForTransactionToComplete(airDropResponse.getAppcoinsTransaction(),
            airDropResponse.getChainId()));
    list.add(transactionService.waitForTransactionToComplete(airDropResponse.getEthTransaction(),
        airDropResponse.getChainId()));
    return Completable.merge(list)
        .andThen(Completable.fromAction(() -> airdropResponse.onNext(
            new AirdropData(AirdropData.AirdropStatus.SUCCESS, airDropResponse.getDescription(),
                airDropResponse.getChainId()))));
  }

  private void publishApiError(AirdropService.AirDropResponse airDropResponse) {
    publish(AirdropData.AirdropStatus.API_ERROR, airDropResponse.getDescription());
  }

  private void publish(AirdropData.AirdropStatus status, String description) {
    airdropResponse.onNext(new AirdropData(status, description));
  }

  private void publish(AirdropData.AirdropStatus status) {
    airdropResponse.onNext(new AirdropData(status));
  }

  public Observable<AirdropData> getStatus() {
    return airdropResponse.filter(
        airdropStatus -> airdropStatus.getStatus() != AirdropData.AirdropStatus.EMPTY);
  }

  public void resetStatus() {
    airdropResponse.onNext(new AirdropData(AirdropData.AirdropStatus.EMPTY));
  }

  public Single<String> requestCaptcha(String walletAddress) {
    return airdropService.requestCaptcha(walletAddress);
  }
}
