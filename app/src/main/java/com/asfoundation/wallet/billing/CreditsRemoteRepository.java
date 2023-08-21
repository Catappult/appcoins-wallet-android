package com.asfoundation.wallet.billing;

import com.appcoins.wallet.bdsbilling.repository.RemoteRepository;
import com.appcoins.wallet.core.network.microservices.model.Transaction;
import io.reactivex.Completable;
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
  public Single<Transaction> pay(@NotNull String walletAddress, @NotNull String signature,
      @NotNull BigDecimal amount, @Nullable String origin, @Nullable String sku,
      @NotNull String type, @NotNull String developerAddress, @Nullable String entityOemId,
      @Nullable String entityDomain, @NotNull String packageName, @Nullable String payload,
      @Nullable String callback, @Nullable String orderReference, @Nullable String referrerUrl,
      @Nullable String productToken) {
    return remoteRepository.registerAuthorizationProof(origin, type, entityOemId, entityDomain,
        null, "appcoins_credits", walletAddress, sku, packageName, amount, developerAddress,
        payload, callback, orderReference, referrerUrl, productToken);
  }

  @NotNull @Override
  public Single<Transaction> sendCredits(@NotNull String toWallet, @NotNull String walletAddress,
      @NotNull String signature, @NotNull BigDecimal amount, @NotNull String origin,
      @NotNull String type, @NotNull String packageName) {
    return remoteRepository.transferCredits(toWallet, origin, type, "appcoins_credits",
        walletAddress, signature, packageName, amount);
  }
}
