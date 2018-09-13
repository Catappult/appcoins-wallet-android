package com.asfoundation.wallet.billing;

import com.appcoins.wallet.billing.repository.RemoteRepository;
import com.appcoins.wallet.billing.repository.entity.TransactionStatus;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public final class BDSTransactionService implements TransactionService {

  private final RemoteRepository remoteRepository;

  public BDSTransactionService(RemoteRepository remoteRepository) {
    this.remoteRepository = remoteRepository;
  }

  @Override public Single<String> createTransaction(String address, String signature, String token,
      String packageName, String payload, String productName, String developerWallet, String storeWallet) {
    return remoteRepository.createAdyenTransaction(address, signature, token, payload, packageName,
        productName, developerWallet, storeWallet)
        .map(TransactionStatus::getUid)
        .subscribeOn(Schedulers.io());
  }

  @Override public Single<String> getSession(String address, String signature, String transactionUid) {
    return remoteRepository.getSessionKey(transactionUid, address, signature)
        .map(authorization -> authorization.getData()
            .getSession())
        .subscribeOn(Schedulers.io());
  }

  @Override public Completable finishTransaction(String address, String signature, String transactionUid, String paykey) {
    return remoteRepository.patchTransaction(transactionUid, address, signature, paykey)
        .subscribeOn(Schedulers.io());
  }
}
