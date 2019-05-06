package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.WalletHistory;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class OffChainTransactionsRepository {
  private final TransactionsApi api;

  public OffChainTransactionsRepository(TransactionsApi api) {
    this.api = api;
  }

  public Single<WalletHistory> getTransactions(String wallet, String versionCode,
      boolean offChainOnly) {
    if (offChainOnly) {
      return api.transactionHistory(wallet, versionCode, "offchain");
    } else {
      return api.transactionHistory(wallet, versionCode);
    }
  }

  public interface TransactionsApi {
    @GET("appc/wallethistory") Single<WalletHistory> transactionHistory(
        @Query("wallet") String wallet, @Query("version_code") String versionCode,
        @Query("type") String transactionType);

    @GET("appc/wallethistory") Single<WalletHistory> transactionHistory(
        @Query("wallet") String wallet, @Query("version_code") String versionCode);
  }
}
