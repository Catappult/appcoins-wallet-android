package cm.aptoide.skills.repository

import javax.inject.Inject
import cm.aptoide.skills.api.TopUpApi
import cm.aptoide.skills.model.TopUpResponse
import cm.aptoide.skills.model.TopUpStatus
import cm.aptoide.skills.model.TransactionType
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

public class TopUpRepository @Inject constructor(private val topUpApi: TopUpApi) {
  fun getTopUpHistory(
    type: TransactionType,
    status: TopUpStatus,
    wallet: String
  ): Single<TopUpResponse> {
    return topUpApi.getTopUpHistory(type, status, wallet).subscribeOn(Schedulers.io())
  }
}