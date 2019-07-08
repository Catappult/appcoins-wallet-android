package com.asfoundation.wallet.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.asfoundation.wallet.repository.entity.TransactionDetailsEntity
import com.asfoundation.wallet.repository.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, TransactionDetailsEntity::class, TransactionDetailsEntity.Icon::class],
    version = 1)
@TypeConverters(TransactionTypeConverter::class)
abstract class TransactionsDatabase : RoomDatabase() {
  abstract fun transactionsDao(): TransactionsDao
}