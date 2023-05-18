package com.asfoundation.wallet.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.appcoins.wallet.core.network.backend.api.TransactionsApi
import com.appcoins.wallet.core.network.backend.model.TransactionResponse
import com.appcoins.wallet.core.network.base.call_adapter.Result
import com.appcoins.wallet.core.network.base.call_adapter.handleApi
import com.asfoundation.wallet.transactions.TransactionModel
import com.asfoundation.wallet.transactions.toModel
import it.czerwinski.android.hilt.annotations.BoundTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

interface TransactionsHistoryRepository {
  suspend fun fetchTransactions(
    wallet: String,
    limit: Int,
    currency: String
  ): Flow<Result<List<TransactionResponse>>>

  fun fetchPagingTransactions(wallet: String, currency: String): TransactionsHistoryPagingSource
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

  override fun fetchPagingTransactions(
    wallet: String,
    currency: String
  ) =
    TransactionsHistoryPagingSource(
      backend = api,
      wallet = wallet,
      currency = currency
    )
}


class TransactionsHistoryPagingSource(
  val backend: TransactionsApi,
  val wallet: String,
  val currency: String
) : PagingSource<String, TransactionModel>() {
  override suspend fun load(params: LoadParams<String>): LoadResult<String, TransactionModel> {
    return try {
      val data = backend.getTransactionHistory(
        wallet = wallet,
        endingDate = params.key,
        limit = params.loadSize,
        defaultCurrency = currency
      ).body()

      LoadResult.Page(
        data = data!!.map { it.toModel(currency) },
        prevKey = null,
        nextKey = data.last().processedTime
      )
    } catch (e: IOException) {
      LoadResult.Error(e)
    } catch (e: HttpException) {
      LoadResult.Error(e)
    }
  }

  override fun getRefreshKey(state: PagingState<String, TransactionModel>): String? {
    val anchorPosition = state.anchorPosition ?: return null
    val transaction = state.closestItemToPosition(anchorPosition) ?: return null
    return transaction.date
  }
}
