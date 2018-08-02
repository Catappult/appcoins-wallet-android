package com.asfoundation.wallet.ws;

import com.asfoundation.wallet.billing.TransactionService;
import com.asfoundation.wallet.ws.transactions.CreateAdyenTransaction;
import com.asfoundation.wallet.ws.transactions.PatchTransaction;
import com.asfoundation.wallet.ws.transactions.TransactionStatus;
import com.asfoundation.wallet.ws.transactions.authorization.GetAuthorization;
import rx.Completable;
import rx.Single;

public class BDSTransactionService implements TransactionService {

  @Override
  public Single<String> createTransaction(String address, String signature, String token) {
    return new CreateAdyenTransaction(token).observe()
        .map(TransactionStatus::getUid)
        .toSingle();
  }

  @Override public Single<String> getSession(String transactionUid) {
    return new GetAuthorization(transactionUid).observe()
        .map(authorization -> authorization.getData()
            .getSession())
        .toSingle();
  }

  @Override public Completable finishTransaction(String transactionUid, String paykey) {
    return new PatchTransaction(transactionUid, paykey).observe()
        .toCompletable();
  }
}
