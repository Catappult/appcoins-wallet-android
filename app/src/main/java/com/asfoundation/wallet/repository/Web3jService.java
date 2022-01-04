package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.PendingTransaction;
import io.reactivex.Single;
import it.czerwinski.android.hilt.annotations.BoundTo;
import javax.inject.Inject;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthTransaction;

/**
 * Created by trinkes on 26/02/2018.
 */
@BoundTo(supertype = EthereumService.class) public class Web3jService implements EthereumService {
  private final Web3jProvider web3j;

  public @Inject Web3jService(Web3jProvider web3j) {
    this.web3j = web3j;
  }

  private Single<PendingTransaction> getTransaction(String hash, Web3j web3jClient) {
    return Single.create(emitter -> {
      try {
        if (!emitter.isDisposed()) {
          EthTransaction ethTransaction = web3jClient.ethGetTransactionByHash(hash)
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

  @Override public Single<PendingTransaction> getTransaction(String hash) {
    return Single.defer(() -> getTransaction(hash, web3j.getDefault()));
  }

  private boolean isPending(EthTransaction ethTransaction) {
    org.web3j.protocol.core.methods.response.Transaction transaction =
        ethTransaction.getTransaction();
    if (transaction == null) {
      throw new TransactionNotFoundException();
    } else {
      return transaction.getBlockNumberRaw() == null;
    }
  }
}
