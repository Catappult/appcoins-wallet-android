package com.asfoundation.wallet.home.usecases

import com.asfoundation.wallet.repository.TransactionsHistoryRepository
import javax.inject.Inject

class FetchTransactionsHistoryUseCase @Inject constructor(private val transactionRepository: TransactionsHistoryRepository) {
  suspend operator fun invoke(wallet: String, quantity: Int = 4) =
    transactionRepository.fetchTransactions(wallet, quantity)
}