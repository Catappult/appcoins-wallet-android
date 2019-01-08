package com.asfoundation.wallet.billing;

import com.appcoins.wallet.bdsbilling.repository.RemoteRepository;
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;

public final class BDSTransactionService implements TransactionService {

  private final RemoteRepository remoteRepository;

  public BDSTransactionService(RemoteRepository remoteRepository) {
    this.remoteRepository = remoteRepository;
  }

  @Override public Single<String> createTransaction(String address, String signature, String token,
      String packageName, String payload, String productName, String developerWallet,
      String storeWallet, String oemWallet, String origin, String walletAddress,
      BigDecimal priceValue, String priceCurrency, String type, String callback,
      String orderReference) {
    return remoteRepository.createAdyenTransaction(origin, walletAddress, signature, token,
        packageName, priceValue, priceCurrency, productName, type, developerWallet, storeWallet,
        oemWallet, payload, callback, orderReference)
        .map(Transaction::getUid)
        .subscribeOn(Schedulers.io());
  }

  @Override
  public Single<String> getSession(String address, String signature, String transactionUid) {
    return remoteRepository.getSessionKey(transactionUid, address, signature)
        .map(authorization -> authorization.getData()
            .getSession())
        .subscribeOn(Schedulers.io());
  }

  @Override
  public Completable finishTransaction(String address, String signature, String transactionUid,
      String paykey) {
    return remoteRepository.patchTransaction(transactionUid, address, signature, paykey)
        .subscribeOn(Schedulers.io());
  }
}
