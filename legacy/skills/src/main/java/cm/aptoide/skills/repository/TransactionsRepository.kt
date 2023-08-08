package cm.aptoide.skills.repository

import com.appcoins.wallet.core.network.backend.api.TransactionApi
import com.appcoins.wallet.core.network.backend.model.TransactionResponse
import com.appcoins.wallet.core.network.backend.model.TransactionType
import javax.inject.Inject
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class TransactionsRepository @Inject constructor(private val transactionApi: TransactionApi) {
  fun getTransactionList(
    wallet: String,
    transactionTypes: List<TransactionType>,
  ): Single<List<TransactionResponse>> {
    return transactionApi.getTransactionList(wallet, transactionTypes).subscribeOn(Schedulers.io())
  }
}