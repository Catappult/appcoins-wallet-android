package com.asfoundation.wallet.billing;

import com.appcoins.wallet.appcoins.rewards.repository.backend.BackendApi;
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository;
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction;
import io.reactivex.Completable;
import io.reactivex.Single;
import java.math.BigDecimal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreditsRemoteRepository
    implements com.appcoins.wallet.appcoins.rewards.repository.RemoteRepository {
  private final BackendApi backendApi;
  private final RemoteRepository remoteRepository;

  public CreditsRemoteRepository(BackendApi backendApi, RemoteRepository remoteRepository) {
    this.backendApi = backendApi;
    this.remoteRepository = remoteRepository;
  }

  @NotNull @Override
  public Single<BackendApi.RewardBalanceResponse> getBalance(@NotNull String address) {
    return backendApi.getBalance(address);
  }

  @NotNull @Override
  public Single<Transaction> pay(@NotNull String walletAddress, @NotNull String signature,
      @NotNull BigDecimal amount, @Nullable String origin, @Nullable String sku,
      @NotNull String type, @NotNull String developerAddress, @NotNull String storeAddress,
      @NotNull String oemAddress, @NotNull String packageName, @Nullable String payload,
      @Nullable String callback, @Nullable String orderReference) {
    return remoteRepository.registerAuthorizationProof(origin, type, oemAddress, null,
        "appcoins_credits", walletAddress, signature, sku, packageName, amount, developerAddress,
        storeAddress, payload, callback, orderReference);
  }

  @NotNull @Override
  public Completable sendCredits(@NotNull String toWallet, @NotNull String walletAddress,
      @NotNull String signature, @NotNull BigDecimal amount, @NotNull String origin,
      @NotNull String type, @NotNull String packageName) {
    return remoteRepository.transferCredits(toWallet, origin, type, "appcoins_credits",
        walletAddress, signature, packageName, amount);
  }
}
