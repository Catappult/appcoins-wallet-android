package com.asfoundation.wallet.repository

import com.appcoins.wallet.core.network.backend.api.TransactionsApi
import com.appcoins.wallet.core.network.backend.model.TransactionResponse
import com.appcoins.wallet.core.network.base.call_adapter.Result
import com.appcoins.wallet.core.network.base.call_adapter.handleApi
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

interface TransactionsHistoryRepository {
  suspend fun fetchTransactions(
    wallet: String,
    limit: Int,
    currency: String
  ): Flow<Result<List<TransactionResponse>>>
}

@BoundTo(supertype = TransactionsHistoryRepository::class)
class DefaultTransactionsHistoryRepository @Inject constructor(private val api: TransactionsApi) :
  TransactionsHistoryRepository {
  override suspend fun fetchTransactions(
    wallet: String,
    limit: Int,
    currency: String
  ): Flow<Result<List<TransactionResponse>>> {

    return flow {
      emit(
        handleApi {
          api.getTransactionHistory(
            wallet = wallet, defaultCurrency = currency, limit = limit
          )
        })
    }
      .flowOn(Dispatchers.IO)
  }
}
