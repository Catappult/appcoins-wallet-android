package com.asfoundation.wallet.repository

import com.appcoins.wallet.core.network.backend.model.TransactionResponse
import com.appcoins.wallet.core.network.backend.remote.RemoteTransactionDataSource
import com.appcoins.wallet.core.network.base.call_adapter.Result
import com.appcoins.wallet.core.network.base.call_adapter.handleApi
import it.czerwinski.android.hilt.annotations.BoundTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

interface TransactionsHistoryRepository {
  suspend fun fetchTransactions(
    wallet: String,
    limit: Int,
    currency: String
  ): Flow<Result<List<TransactionResponse>>>
}

@BoundTo(supertype = TransactionsHistoryRepository::class)
class DefaultTransactionsHistoryRepository @Inject constructor(
  private val remoteTransactionDataSource: RemoteTransactionDataSource
) : TransactionsHistoryRepository {
  override suspend fun fetchTransactions(
    wallet: String, limit: Int, currency: String
  ): Flow<Result<List<TransactionResponse>>> {

    return flow {
      emit(handleApi {
        remoteTransactionDataSource.getTransactionsHistory(
          wallet,
          selectedCurrency = currency,
          limit
        )
      })
    }.flowOn(Dispatchers.IO)
  }
}
