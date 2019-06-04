package com.asfoundation.wallet.repository

import androidx.room.*
import com.asfoundation.wallet.repository.entity.TransactionEntity
import io.reactivex.Flowable
import io.reactivex.Maybe

@Dao
interface TransactionsDao {
  @Query("select * from TransactionEntity where transactionId like :key")
  fun getSyncTransaction(key: String): TransactionEntity?

  @Query(
      "select * from TransactionEntity where transactionId like :key")
  fun getTransaction(key: String): Flowable<TransactionEntity>

  @Query(
      "select * from TransactionEntity where relatedWallet like :relatedWallet order by timeStamp")
  fun getAllAsFlowable(relatedWallet: String): Flowable<List<TransactionEntity>>

  @Query("select * from TransactionEntity order by timeStamp")
  fun getAll(): List<TransactionEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(roomTransaction: TransactionEntity)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  fun insertAll(roomTransactions: List<TransactionEntity>)

  @Delete
  fun remove(roomTransaction: TransactionEntity?)

  @Query(
      "select * from TransactionEntity where relatedWallet like :relatedWallet order by timeStamp desc limit 1")
  fun getNewestTransaction(relatedWallet: String): Maybe<TransactionEntity>

  @Query(
      "select * from TransactionEntity where relatedWallet like :relatedWallet order by timeStamp asc limit 1")
  fun getOlderTransaction(relatedWallet: String): Maybe<TransactionEntity>
}