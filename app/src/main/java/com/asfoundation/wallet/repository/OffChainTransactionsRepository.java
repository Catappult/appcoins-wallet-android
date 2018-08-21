package com.asfoundation.wallet.repository;

import com.bds.microraidenj.ws.ChannelHistoryResponse;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class OffChainTransactionsRepository {
  private final TransactionsApi api;

  public OffChainTransactionsRepository(TransactionsApi api) {
    this.api = api;
  }

  public Single<ChannelHistoryResponse> getTransactions(String wallet) {
    return api.transactionHistory(wallet, "iap_offchain");
  }

  public interface TransactionsApi {
    @GET("appc/wallethistory") Single<ChannelHistoryResponse> transactionHistory(
        @Query("wallet") String wallet, @Query("type") String transactionType);
  }
}
