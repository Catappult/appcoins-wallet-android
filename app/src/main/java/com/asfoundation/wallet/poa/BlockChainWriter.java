package com.asfoundation.wallet.poa;

import com.asfoundation.wallet.repository.TransactionException;
import com.asfoundation.wallet.repository.Web3jProvider;
import com.google.gson.Gson;
import io.reactivex.Single;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

public class BlockChainWriter {
  private final Web3jProvider web3jProvider;
  private final TransactionFactory transactionFactory;
  private final Gson gson;

  public BlockChainWriter(Web3jProvider web3jProvider, TransactionFactory transactionFactory,
      Gson gson) {
    this.web3jProvider = web3jProvider;
    this.transactionFactory = transactionFactory;
    this.gson = gson;
  }

  public Single<String> writeProof(Proof proof) {
    return transactionFactory.createProofTransaction(gson.toJson(proof))
        .flatMap(this::sendTransaction);
  }

  private Single<String> sendTransaction(byte[] transaction) {
    return Single.fromCallable(() -> {
      EthSendTransaction sentTransaction = web3jProvider.get()
          .ethSendRawTransaction(Numeric.toHexString(transaction))
          .send();
      if (sentTransaction.hasError()) {
        throw new TransactionException(sentTransaction.getError()
            .getCode(), sentTransaction.getError()
            .getMessage(), sentTransaction.getError()
            .getData());
      }
      return sentTransaction.getTransactionHash();
    });
  }
}
