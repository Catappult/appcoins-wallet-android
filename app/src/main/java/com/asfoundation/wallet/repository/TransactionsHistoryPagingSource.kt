package com.asfoundation.wallet.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.appcoins.wallet.core.network.backend.api.TransactionsApi
import com.appcoins.wallet.core.network.backend.model.TransactionResponse
import retrofit2.HttpException
import java.io.IOException
import java.util.Locale


class TransactionsHistoryPagingSource(
  val backend: TransactionsApi,
  val wallet: String,
  val currency: String
) : PagingSource<String, TransactionResponse>() {

  override suspend fun load(params: LoadParams<String>): LoadResult<String, TransactionResponse> {
    return try {
      val response = backend.getTransactionHistory(
        wallet = wallet,
        cursor = params.key, // Use the cursor from params.key
        limit = params.loadSize,
        defaultCurrency = currency,
        languageCode = Locale.getDefault().language
      ).body()

      val items = response?.items ?: emptyList()
      val nextCursor = response?.nextCursor

      LoadResult.Page(
        data = items,
        prevKey = null, // no paging backward
        nextKey = nextCursor
      )
    } catch (e: IOException) {
      LoadResult.Error(e)
    } catch (e: HttpException) {
      LoadResult.Error(e)
    } catch (e: Exception) {
      LoadResult.Error(e)
    }
  }

  override fun getRefreshKey(state: PagingState<String, TransactionResponse>): String? {
    return null
  }
}