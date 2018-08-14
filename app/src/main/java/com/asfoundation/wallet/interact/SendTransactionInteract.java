package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.PasswordStore;
import com.asfoundation.wallet.repository.TransactionRepositoryType;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import java.math.BigInteger;

public class SendTransactionInteract {

  private final TransactionRepositoryType transactionRepository;
  private final PasswordStore passwordStore;

  public SendTransactionInteract(TransactionRepositoryType transactionRepository,
      PasswordStore passwordStore) {
    this.transactionRepository = transactionRepository;
    this.passwordStore = passwordStore;
  }

  public Single<String> send(TransactionBuilder transactionBuilder) {
    return passwordStore.getPassword(new Wallet(transactionBuilder.fromAddress()))
        .flatMap(password -> transactionRepository.createTransaction(transactionBuilder, password)
            .observeOn(AndroidSchedulers.mainThread()));
  }

  public Single<String> approve(TransactionBuilder transactionBuilder, BigInteger nonce) {
    return passwordStore.getPassword(new Wallet(transactionBuilder.fromAddress()))
        .flatMap(password -> transactionRepository.approve(transactionBuilder, password, nonce));
  }

  public Single<String> buy(TransactionBuilder transaction, BigInteger nonce) {
    return passwordStore.getPassword(new Wallet(transaction.fromAddress()))
        .flatMap(password -> transactionRepository.callIab(transaction, password, nonce));
  }

  public Single<String> computeApproveTransactionHash(TransactionBuilder transactionBuilder,
      BigInteger nonce) {
    return passwordStore.getPassword(new Wallet(transactionBuilder.fromAddress()))
        .flatMap(password -> transactionRepository.computeApproveTransactionHash(transactionBuilder,
            password, nonce));
  }

  public Single<String> computeBuyTransactionHash(TransactionBuilder transactionBuilder,
      BigInteger nonce) {
    return passwordStore.getPassword(new Wallet(transactionBuilder.fromAddress()))
        .flatMap(password -> transactionRepository.computeBuyTransactionHash(transactionBuilder,
            password, nonce));
  }
}
