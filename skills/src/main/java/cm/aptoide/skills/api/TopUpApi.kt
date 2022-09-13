package cm.aptoide.skills.api;

import cm.aptoide.skills.model.TopUpResponse;
import cm.aptoide.skills.model.TopUpStatus
import cm.aptoide.skills.model.TransactionType
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface TopUpApi {
  @GET("8.20200101/transactions")
  fun getTopUpHistory(
    @Query("type") type: TransactionType,
    @Query("status") topUpStatus: TopUpStatus,
    @Query("wallet_from") walletAddress: String
  ): Single<TopUpResponse>
}