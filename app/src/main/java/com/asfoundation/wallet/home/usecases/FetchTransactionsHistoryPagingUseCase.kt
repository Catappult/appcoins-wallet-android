package com.asfoundation.wallet.home.usecases

import com.asfoundation.wallet.repository.TransactionsHistoryRepository
import javax.inject.Inject

class FetchTransactionsHistoryPagingUseCase @Inject constructor(private val transactionRepository: TransactionsHistoryRepository) {
  operator fun invoke(wallet: String, currency: String) =
    transactionRepository.fetchPagingTransactions(wallet, currency)
}