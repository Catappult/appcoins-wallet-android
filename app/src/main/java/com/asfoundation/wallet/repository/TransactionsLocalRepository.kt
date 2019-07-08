package com.asfoundation.wallet.repository

import android.content.SharedPreferences
import com.asfoundation.wallet.repository.entity.TransactionEntity
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single

class TransactionsLocalRepository(private val database: TransactionsDao,
                                  private val sharedPreferences: SharedPreferences) :
    TransactionsRepository {

  companion object {
    private const val OLD_TRANSACTIONS_LOAD = "IS_OLD_TRANSACTIONS_LOADED"
  }


  override fun getAllAsFlowable(relatedWallet: String): Flowable<List<TransactionEntity>> {
    return database.getAllAsFlowable(relatedWallet)
  }

  override fun insertAll(roomTransactions: List<TransactionEntity>) {
    return database.insertAll(roomTransactions)
  }

  override fun getNewestTransaction(relatedWallet: String): Maybe<TransactionEntity> {
    return database.getNewestTransaction(relatedWallet)

  }

  override fun getOlderTransaction(relatedWallet: String): Maybe<TransactionEntity> {
    return database.getOlderTransaction(relatedWallet)

  }


  override fun isOldTransactionsLoaded(): Single<Boolean> {
    return Single.fromCallable { sharedPreferences.getBoolean(OLD_TRANSACTIONS_LOAD, false) }
  }

  override fun oldTransactionsLoaded() {
    sharedPreferences.edit()
        .putBoolean(OLD_TRANSACTIONS_LOAD, true)
        .apply()
  }
}