package com.asfoundation.wallet.home.usecases

import com.asfoundation.wallet.rating.RatingRepository
import com.asfoundation.wallet.transactions.Transaction

class UpdateTransactionsNumberUseCase(private val ratingRepository: RatingRepository) {

  companion object {
    const val MINIMUM_TRANSACTIONS_NR = 5
  }

  operator fun invoke(transactions: List<Transaction>) {
    var transactionsNumber = 0
    for (transaction in transactions) {
      if ((transaction.type == Transaction.TransactionType.IAP
              || transaction.type == Transaction.TransactionType.TOP_UP
              || transaction.type == Transaction.TransactionType.IAP_OFFCHAIN)
          && transaction.status == Transaction.TransactionStatus.SUCCESS) {
        if (++transactionsNumber >= MINIMUM_TRANSACTIONS_NR) {
          ratingRepository.saveEnoughSuccessfulTransactions()
          break
        }
      }
    }
  }
}