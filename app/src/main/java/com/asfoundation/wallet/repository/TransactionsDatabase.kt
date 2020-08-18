package com.asfoundation.wallet.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.asfoundation.wallet.repository.entity.TransactionDetailsEntity
import com.asfoundation.wallet.repository.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, TransactionDetailsEntity::class, TransactionDetailsEntity.Icon::class],
    version = 4)
@TypeConverters(TransactionTypeConverter::class)
abstract class TransactionsDatabase : RoomDatabase() {

  abstract fun transactionsDao(): TransactionsDao

  companion object {

    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
      override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS TransactionEntityCopy (transactionId TEXT NOT "
                + "NULL, relatedWallet TEXT NOT NULL, approveTransactionId TEXT, type TEXT NOT "
                + "NULL, timeStamp INTEGER NOT NULL, processedTime INTEGER NOT NULL, status "
                + "TEXT NOT NULL, value TEXT NOT NULL, `from` TEXT NOT NULL, `to` TEXT NOT NULL, "
                + "currency TEXT, operations TEXT, sourceName TEXT, description TEXT, "
                + "iconType TEXT, uri TEXT, PRIMARY KEY(transactionId, relatedWallet))")
        database.execSQL("INSERT INTO TransactionEntityCopy (transactionId, relatedWallet, "
            + "approveTransactionId, type, timeStamp, processedTime, status, value, `from`, `to`,"
            + " currency, operations, sourceName, description, iconType, uri) SELECT "
            + "transactionId, relatedWallet,approveTransactionId, type, timeStamp, processedTime,"
            + " status, value, `from`, `to`, currency, operations, sourceName, description, "
            + "iconType, uri FROM TransactionEntity")
        database.execSQL("DROP TABLE TransactionEntity")
        database.execSQL("ALTER TABLE TransactionEntityCopy RENAME TO TransactionEntity")
      }
    }

    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
      override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DELETE FROM TransactionEntity")
        database.execSQL("DELETE FROM TransactionDetailsEntity")
        database.execSQL("DELETE FROM Icon")
      }
    }

    //Adds 3 new values to the object related to promotions
    val MIGRATION_3_4: Migration = object : Migration(3, 4) {
      override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE TransactionEntity ADD COLUMN subType TEXT")
        database.execSQL("ALTER TABLE TransactionEntity ADD COLUMN title TEXT")
        database.execSQL("ALTER TABLE TransactionEntity ADD COLUMN cardDescription TEXT")
      }
    }
  }
}