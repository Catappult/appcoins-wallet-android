package com.asfoundation.wallet.home.usecases

import com.asfoundation.wallet.repository.TransactionRepositoryType
import com.asfoundation.wallet.transactions.Transaction
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FetchTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepositoryType) {

  operator fun invoke(wallet: String): Observable<List<Transaction>> {
    return transactionRepository.fetchTransactions(wallet)
      .subscribeOn(Schedulers.io())
      .doAfterTerminate { transactionRepository.stop() }
  }
}