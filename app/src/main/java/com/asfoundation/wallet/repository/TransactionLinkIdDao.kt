package com.asfoundation.wallet.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.asfoundation.wallet.repository.entity.TransactionLinkIdEntity
import io.reactivex.Single

@Dao
interface TransactionLinkIdDao {

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  fun insert(vararg: TransactionLinkIdEntity)

  @Query("select * from transaction_link_id where transactionId  = :transactionId limit 1")
  fun getRevertedTransaction(transactionId: String): Single<TransactionLinkIdEntity>

  @Query("select * from transaction_link_id where linkTransactionId  = :transactionId limit 1")
  fun getRevertTransaction(transactionId: String): Single<TransactionLinkIdEntity>

}