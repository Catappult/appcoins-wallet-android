package com.appcoins.wallet.core.network.backend

import com.appcoins.wallet.core.network.backend.model.TransactionResponse
import retrofit2.Response

interface BackendDataSource {
  suspend fun getTransactionsHistory(
    wallet: String,
    selectedCurrency: String,
    limit: Int
  ): Response<List<TransactionResponse>>
}