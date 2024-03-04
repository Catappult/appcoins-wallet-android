package cm.aptoide.skills.repository


import com.appcoins.wallet.core.network.backend.api.TransactionOverviewApi
import com.appcoins.wallet.core.network.backend.model.BackendTransactionType
import com.appcoins.wallet.core.network.backend.model.TransactionOverviewResponse
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class TransactionsRepository @Inject constructor(private val transactionOverviewApi: TransactionOverviewApi) {
  fun getTransactionList(
    wallet: String? = null,
    transactionTypes: List<BackendTransactionType>? = null,
    limit: Int? = null,
    offset: Int? = null,
  ): Single<List<TransactionOverviewResponse>> {
    return transactionOverviewApi.getTransactionOverviewList(
      wallet,
      transactionTypes,
      limit,
      offset
    ).subscribeOn(Schedulers.io())
  }
}