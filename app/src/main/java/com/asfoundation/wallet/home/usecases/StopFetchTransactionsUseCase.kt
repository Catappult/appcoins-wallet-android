package com.asfoundation.wallet.home.usecases

import com.asfoundation.wallet.repository.TransactionRepositoryType

class StopFetchTransactionsUseCase(private val transactionRepository: TransactionRepositoryType) {

  operator fun invoke() {
    transactionRepository.stop()
  }
}