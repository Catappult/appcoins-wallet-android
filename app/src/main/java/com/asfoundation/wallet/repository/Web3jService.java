package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.PendingTransaction;
import io.reactivex.Single;
import org.web3j.protocol.core.methods.response.EthTransaction;

/**
 * Created by trinkes on 26/02/2018.
 */

public class Web3jService implements EthereumService {
  private final Web3jProvider web3j;

  public Web3jService(Web3jProvider web3j) {
    this.web3j = web3j;
  }

  @Override public Single<PendingTransaction> getTransaction(String hash) {
    return Single.create(emitter -> {
      try {
        if (!emitter.isDisposed()) {
          EthTransaction ethTransaction = web3j.get()
              .ethGetTransactionByHash(hash)
              .send();
          if (ethTransaction.hasError()) {
            emitter.onError(new RuntimeException(ethTransaction.getError()
                .getMessage()));
          } else {
            emitter.onSuccess(new PendingTransaction(hash, isPending(ethTransaction)));
          }
        }
      } catch (Exception e) {
        if (!emitter.isDisposed()) {
          emitter.onError(e);
        }
      }
    });
  }

  private boolean isPending(EthTransaction ethTransaction) {
    org.web3j.protocol.core.methods.response.Transaction transaction =
        ethTransaction.getTransaction();
    return transaction.getBlockNumberRaw() == null;
  }
}
