package com.asfoundation.wallet.repository

import com.asfoundation.wallet.repository.entity.TransactionEntity
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single

interface TransactionsRepository {
  fun getAllAsFlowable(relatedWallet: String): Flowable<List<TransactionEntity>>
  fun insertAll(roomTransactions: List<TransactionEntity>)
  fun getNewestTransaction(relatedWallet: String): Maybe<TransactionEntity>
  fun getOlderTransaction(relatedWallet: String): Maybe<TransactionEntity>
  fun isOldTransactionsLoaded(): Single<Boolean>
  fun oldTransactionsLoaded()
}
