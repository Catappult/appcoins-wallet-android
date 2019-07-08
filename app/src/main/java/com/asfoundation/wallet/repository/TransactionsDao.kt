package com.asfoundation.wallet.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.asfoundation.wallet.repository.entity.TransactionEntity
import io.reactivex.Flowable
import io.reactivex.Maybe

@Dao
interface TransactionsDao {

  @Query(
      "select * from TransactionEntity where relatedWallet like :relatedWallet order by timeStamp")
  fun getAllAsFlowable(relatedWallet: String): Flowable<List<TransactionEntity>>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  fun insertAll(roomTransactions: List<TransactionEntity>)

  @Query(
      "select * from TransactionEntity where relatedWallet like :relatedWallet order by processedTime desc limit 1")
  fun getNewestTransaction(relatedWallet: String): Maybe<TransactionEntity>

  @Query(
      "select * from TransactionEntity where relatedWallet like :relatedWallet order by processedTime asc limit 1")
  fun getOlderTransaction(relatedWallet: String): Maybe<TransactionEntity>
}