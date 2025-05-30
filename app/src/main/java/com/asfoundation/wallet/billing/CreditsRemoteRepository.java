package com.asfoundation.wallet.billing;

import com.appcoins.wallet.bdsbilling.repository.RemoteRepository;
import com.appcoins.wallet.core.network.microservices.model.Transaction;
import io.reactivex.Single;
import java.math.BigDecimal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreditsRemoteRepository
    implements com.appcoins.wallet.appcoins.rewards.repository.RemoteRepository {
  private final RemoteRepository remoteRepository;

  public CreditsRemoteRepository(RemoteRepository remoteRepository) {
    this.remoteRepository = remoteRepository;
  }

  @NotNull @Override
  public Single<Transaction> pay(
      @NotNull String walletAddress,
      @NotNull String signature,
      @NotNull BigDecimal amount,
      @Nullable String origin,
      @Nullable String sku,
      @NotNull String type,
      @Nullable String entityOemId,
      @Nullable String entityDomain,
      @NotNull String packageName,
      @Nullable String payload,
      @Nullable String callback,
      @Nullable String orderReference,
      @Nullable String referrerUrl,
      String guestWalletId
  ) {
    return remoteRepository.registerAuthorizationProof(
        origin,
        type,
        entityOemId,
        entityDomain,
        null,
        "appcoins_credits",
        walletAddress,
        sku,
        packageName,
        amount,
        payload,
        callback,
        orderReference,
        referrerUrl,
        guestWalletId
    );
  }

  @NotNull @Override
  public Single<Transaction> sendCredits(
      @NotNull String toWallet,
      @NotNull String walletAddress,
      @NotNull BigDecimal amount,
      @NotNull String currency,
      @NotNull String origin,
      @NotNull String type,
      @NotNull String packageName,
      String guestWalletId
  ) {
    return remoteRepository.transferCredits(
        toWallet,
        origin,
        type,
        "appcoins_credits",
        walletAddress,
        packageName,
        amount,
        currency,
        guestWalletId
    );
  }
}
