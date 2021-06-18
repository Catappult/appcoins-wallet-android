package com.asfoundation.wallet.home.usecases

import com.asfoundation.wallet.repository.TransactionRepositoryType
import com.asfoundation.wallet.transactions.Transaction
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class FetchTransactionsUseCase(private val transactionRepository: TransactionRepositoryType) {

  operator fun invoke(wallet: String): Observable<List<Transaction>> {
    return transactionRepository.fetchTransaction(wallet)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
  }
}