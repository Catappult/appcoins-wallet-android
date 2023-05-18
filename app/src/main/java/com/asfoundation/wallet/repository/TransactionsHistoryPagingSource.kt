package com.asfoundation.wallet.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.appcoins.wallet.core.network.backend.api.TransactionsApi
import com.asfoundation.wallet.transactions.TransactionModel
import com.asfoundation.wallet.transactions.toModel
import retrofit2.HttpException
import java.io.IOException


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