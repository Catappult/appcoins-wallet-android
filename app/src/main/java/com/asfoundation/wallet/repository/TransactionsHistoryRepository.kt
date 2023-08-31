package com.asfoundation.wallet.repository

import com.appcoins.wallet.core.network.backend.api.TransactionsApi
import com.appcoins.wallet.core.network.backend.model.InvoiceResponse
import com.appcoins.wallet.core.network.backend.model.TransactionResponse
import com.appcoins.wallet.core.network.base.call_adapter.Result
import com.appcoins.wallet.core.network.base.call_adapter.handleApi
import it.czerwinski.android.hilt.annotations.BoundTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

interface TransactionsHistoryRepository {
  fun fetchTransactions(
    wallet: String,
    limit: Int,
    currency: String
  ): Flow<Result<List<TransactionResponse>>>

  fun fetchPagingTransactions(wallet: String, currency: String): TransactionsHistoryPagingSource

  fun getInvoiceUrl(invoiceId: String, ewt: String): Flow<Result<InvoiceResponse>>
}

@BoundTo(supertype = TransactionsHistoryRepository::class)
class DefaultTransactionsHistoryRepository @Inject constructor(private val api: TransactionsApi) :
  TransactionsHistoryRepository {
  override fun fetchTransactions(
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

  override fun fetchPagingTransactions(wallet: String, currency: String) =
    TransactionsHistoryPagingSource(backend = api, wallet = wallet, currency = currency)

  override fun getInvoiceUrl(invoiceId: String, ewt: String): Flow<Result<InvoiceResponse>> {
    return flow {
      emit(handleApi { api.getInvoiceById(invoiceId = invoiceId, authorization = ewt) })
    }
      .flowOn(Dispatchers.IO)
  }
}
