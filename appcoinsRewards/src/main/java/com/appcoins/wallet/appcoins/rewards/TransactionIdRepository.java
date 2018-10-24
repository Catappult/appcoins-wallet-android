package com.appcoins.wallet.appcoins.rewards;

import com.appcoins.wallet.bdsbilling.repository.GetTransactionIdResponse;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class TransactionIdRepository {

  private final Api api;

  public TransactionIdRepository(Api api) {
    this.api = api;
  }

  public Single<String> getTransactionUid(String uid) {
    return api.getTransactionId(uid)
        .map(GetTransactionIdResponse::getTxid);
  }

  public interface Api {

    @GET("appc/transaction") Single<GetTransactionIdResponse> getTransactionId(
        @Query("uid") String uid);
  }
}

