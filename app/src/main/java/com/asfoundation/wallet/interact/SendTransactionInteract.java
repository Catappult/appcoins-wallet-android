package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.entity.TransactionBuilder;
import com.appcoins.wallet.feature.walletInfo.data.authentication.PasswordStore;
import com.asfoundation.wallet.repository.TransactionRepositoryType;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;

public class SendTransactionInteract {

  private final TransactionRepositoryType transactionRepository;
  private final PasswordStore passwordStore;

  public @Inject SendTransactionInteract(TransactionRepositoryType transactionRepository,
      PasswordStore passwordStore) {
    this.transactionRepository = transactionRepository;
    this.passwordStore = passwordStore;
  }

  public Single<String> send(TransactionBuilder transactionBuilder) {
    return passwordStore.getPassword(transactionBuilder.fromAddress())
        .subscribeOn(Schedulers.io())
        .flatMap(password -> transactionRepository.createTransaction(transactionBuilder, password));
  }

  public Single<String> approve(TransactionBuilder transactionBuilder) {
    return passwordStore.getPassword(transactionBuilder.fromAddress())
        .flatMap(password -> transactionRepository.approve(transactionBuilder, password));
  }

  public Single<String> buy(TransactionBuilder transaction) {
    return passwordStore.getPassword(transaction.fromAddress())
        .flatMap(password -> transactionRepository.callIab(transaction, password));
  }

  public Single<String> computeApproveTransactionHash(TransactionBuilder transactionBuilder) {
    return passwordStore.getPassword(transactionBuilder.fromAddress())
        .flatMap(password -> transactionRepository.computeApproveTransactionHash(transactionBuilder,
            password));
  }

  public Single<String> computeBuyTransactionHash(TransactionBuilder transactionBuilder) {
    return passwordStore.getPassword(transactionBuilder.fromAddress())
        .flatMap(password -> transactionRepository.computeBuyTransactionHash(transactionBuilder,
            password));
  }
}
