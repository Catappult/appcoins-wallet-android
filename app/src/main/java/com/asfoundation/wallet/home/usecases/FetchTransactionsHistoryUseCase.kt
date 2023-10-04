package com.asfoundation.wallet.home.usecases

import com.asfoundation.wallet.repository.TransactionsHistoryRepository
import javax.inject.Inject

class FetchTransactionsHistoryUseCase
@Inject
constructor(private val transactionRepository: TransactionsHistoryRepository) {
  operator fun invoke(wallet: String, limit: Int, currency: String) =
    transactionRepository.fetchTransactions(wallet, limit, currency)
}
