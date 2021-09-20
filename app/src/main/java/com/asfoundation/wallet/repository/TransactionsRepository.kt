package com.asfoundation.wallet.repository

import com.asfoundation.wallet.repository.entity.LastUpdatedWalletEntity
import com.asfoundation.wallet.repository.entity.TransactionEntity
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single

interface TransactionsRepository {

  fun getAllAsFlowable(relatedWallet: String): Flowable<List<TransactionEntity>>

  fun setTransactionsLastUpdated(wallet: String, timestamp: Long)

  fun getWalletLastUpdated(wallet: String): Flowable<LastUpdatedWalletEntity>

  fun insertAll(roomTransactions: List<TransactionEntity>)

  fun getNewestTransaction(relatedWallet: String): Maybe<TransactionEntity>

  fun getOlderTransaction(relatedWallet: String): Maybe<TransactionEntity>

  fun isOldTransactionsLoaded(): Single<Boolean>

  fun oldTransactionsLoaded()

  fun deleteAllTransactions()

  fun setLocale(locale: String)

  fun getLastLocale(): String?

  fun insertTransactionLink(revertTxId: String, originalTxId: String)

  fun getRevertedTransaction(wallet: String, txId: String): Single<TransactionEntity>

  fun getRevertTransaction(wallet: String, txId: String): Single<TransactionEntity>

  fun getRevertedTxId(wallet: String, txId: String): Single<String>
}
