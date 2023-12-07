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
      val data = backend.getTransactionHistory(
        wallet = wallet,
        endingDate = params.key,
        limit = params.loadSize,
        defaultCurrency = currency,
        languageCode = Locale.getDefault().language
      ).body()

      LoadResult.Page(
        data = data!!.map { it },
        prevKey = null,
        nextKey = data.lastOrNull()?.processedTime
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
    val anchorPosition = state.anchorPosition ?: return null
    val transaction = state.closestItemToPosition(anchorPosition) ?: return null
    return transaction.processedTime
  }
}