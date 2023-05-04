package com.appcoins.wallet.core.network.backend.remote

import com.appcoins.wallet.core.network.backend.BackendDataSource
import com.appcoins.wallet.core.network.backend.api.TransactionsApi
import javax.inject.Inject

class RemoteTransactionDataSource @Inject constructor(private val api: TransactionsApi) :
  BackendDataSource {

  override suspend fun getTransactionsHistory(
    wallet: String,
    selectedCurrency: String,
    limit: Int
  ) = api.getTransactionHistory(wallet = wallet, defaultCurrency = selectedCurrency, limit = limit)
}