package cm.aptoide.skills.repository

import com.appcoins.wallet.core.network.backend.api.TransactionOverviewApi
import com.appcoins.wallet.core.network.backend.model.TransactionOverviewResponse
import com.appcoins.wallet.core.network.backend.model.BackendTransactionType
import javax.inject.Inject
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class TransactionsRepository @Inject constructor(private val transactionOverviewApi: TransactionOverviewApi) {
  fun getTransactionList(
    wallet: String? = null,
    transactionTypes: List<BackendTransactionType>? = null,
    limit: Int? = null,
    offset: Int? = null,
  ): Single<List<TransactionOverviewResponse>> {
    return transactionOverviewApi.getTransactionOverviewList(wallet, transactionTypes, limit, offset).subscribeOn(Schedulers.io())
  }
}