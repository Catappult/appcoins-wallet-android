package cm.aptoide.skills.repository

import javax.inject.Inject
import com.appcoins.wallet.core.network.microservices.model.TopUpResponse
import com.appcoins.wallet.core.network.microservices.model.TopUpStatus
import com.appcoins.wallet.core.network.microservices.model.TransactionType
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