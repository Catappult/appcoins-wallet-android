package com.asfoundation.wallet.repository

import com.appcoins.wallet.core.network.backend.ApiResult
import com.appcoins.wallet.core.network.backend.handleApi
import com.appcoins.wallet.core.network.backend.model.TransactionResponse
import com.appcoins.wallet.core.network.backend.remote.RemoteTransactionDataSource
import it.czerwinski.android.hilt.annotations.BoundTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

interface TransactionsHistoryRepository {
  suspend fun fetchTransactions(
    wallet: String,
    quantity: Int
  ): Flow<ApiResult<List<TransactionResponse>>>
}

@BoundTo(supertype = TransactionsHistoryRepository::class)
class DefaultTransactionsHistoryRepository @Inject constructor(
  private val remoteTransactionDataSource: RemoteTransactionDataSource
) : TransactionsHistoryRepository {
  override suspend fun fetchTransactions(
    wallet: String, quantity: Int
  ): Flow<ApiResult<List<TransactionResponse>>> {
    return flow {
      emit(handleApi { remoteTransactionDataSource.getTransactionsHistory(wallet) })
    }.flowOn(Dispatchers.IO)
  }
}
