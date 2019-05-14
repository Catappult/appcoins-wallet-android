package com.asfoundation.wallet.repository

import androidx.room.*
import com.asfoundation.wallet.repository.entity.TransactionEntity
import io.reactivex.Flowable

@Dao
interface TransactionsDao {
  @Query("select * from TransactionEntity where `transactionId` like :key")
  fun getSyncTransaction(key: String): TransactionEntity?

  @Query(
      "select * from TransactionEntity where `transactionId` like :key")
  fun getTransaction(key: String): Flowable<TransactionEntity>

  @Query(
      "select * from TransactionEntity where relatedWallet like :relatedWallet order by `timeStamp`")
  fun getAllAsFlowable(relatedWallet: String): Flowable<List<TransactionEntity>>

  @Query("select * from TransactionEntity order by `timeStamp`")
  fun getAll(): List<TransactionEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(roomTransaction: TransactionEntity)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  fun insertAll(roomTransactions: List<TransactionEntity>)

  @Delete
  fun remove(roomTransaction: TransactionEntity?)

}