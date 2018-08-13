package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.SendTransactionInteract;
import io.reactivex.Single;
import java.math.BigInteger;
import org.jetbrains.annotations.NotNull;

public class ApproveTransactionSender implements TransactionSender {
  private final SendTransactionInteract sendTransactionInteract;

  public ApproveTransactionSender(SendTransactionInteract sendTransactionInteract) {
    this.sendTransactionInteract = sendTransactionInteract;
  }

  @NotNull @Override public Single<String> send(@NotNull TransactionBuilder transactionBuilder,
      @NotNull BigInteger nonce) {
    return sendTransactionInteract.approve(transactionBuilder, nonce);
  }
}
