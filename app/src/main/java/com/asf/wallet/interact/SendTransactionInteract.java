package com.asf.wallet.interact;

import com.asf.wallet.entity.TransactionBuilder;
import com.asf.wallet.entity.Wallet;
import com.asf.wallet.repository.PasswordStore;
import com.asf.wallet.repository.TransactionRepositoryType;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;

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
}
