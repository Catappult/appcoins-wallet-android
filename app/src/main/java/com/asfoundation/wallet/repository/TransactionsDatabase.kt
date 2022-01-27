package com.asfoundation.wallet.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.asfoundation.wallet.repository.entity.LastUpdatedWalletEntity
import com.asfoundation.wallet.repository.entity.TransactionDetailsEntity
import com.asfoundation.wallet.repository.entity.TransactionEntity
import com.asfoundation.wallet.repository.entity.TransactionLinkIdEntity

@Database(
    entities = [
      TransactionEntity::class,
      TransactionDetailsEntity::class,
      TransactionDetailsEntity.Icon::class,
      TransactionLinkIdEntity::class,
      LastUpdatedWalletEntity::class
    ],
    version = 9)
@TypeConverters(TransactionTypeConverter::class)
abstract class TransactionsDatabase : RoomDatabase() {

  abstract fun transactionsDao(): TransactionsDao
  abstract fun transactionLinkIdDao(): TransactionLinkIdDao

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

    //Adds 3 new values to the object related to the perk promotions
    val MIGRATION_3_4: Migration = object : Migration(3, 4) {
      override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE TransactionEntity ADD COLUMN perk TEXT")
        database.execSQL("ALTER TABLE TransactionEntity ADD COLUMN subType TEXT")
        database.execSQL("ALTER TABLE TransactionEntity ADD COLUMN title TEXT")
        database.execSQL("ALTER TABLE TransactionEntity ADD COLUMN cardDescription TEXT")
        //Delete rows created after 14th September 2020 12:00 so that every perk bonus
        // transactions that happened after this is converted into the layout of the perk bonus
        // processed time has milliseconds into account
        database.execSQL("DELETE FROM TransactionEntity WHERE processedTime >= 1600084800000")
      }
    }

    val MIGRATION_4_5: Migration = object : Migration(4, 5) {
      override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS transaction_link_id (id INTEGER PRIMARY KEY AUTOINCREMENT, transactionId TEXT NOT NULL, linkTransactionId TEXT NOT NULL)")
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_transaction_link_id_transactionId_linkTransactionId ON transaction_link_id (transactionId, linkTransactionId)")
        database.execSQL("DELETE FROM TransactionEntity WHERE processedTime >= 1583280000000")
      }
    }

    //Adds 2 new values to be possible to show fiat on transactions
    val MIGRATION_5_6: Migration = object : Migration(5, 6) {
      override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DELETE FROM TransactionEntity")
        database.execSQL("ALTER TABLE TransactionEntity ADD COLUMN paidAmount TEXT")
        database.execSQL("ALTER TABLE TransactionEntity ADD COLUMN paidCurrency TEXT")
      }
    }

    val MIGRATION_6_7: Migration = object : Migration(6, 7) {
      override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS LastUpdatedWalletEntity (wallet TEXT NOT NULL, transactionsUpdateTimestamp INTEGER NOT NULL, PRIMARY KEY(wallet))")
      }
    }

    // Adds new column to be possible to show the order game reference on transactions
    // Also adds column for method
    val MIGRATION_7_8: Migration = object : Migration(7, 8) {
      override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE TransactionEntity ADD COLUMN orderReference TEXT")
        database.execSQL("ALTER TABLE TransactionEntity ADD COLUMN method TEXT")
      }
    }

  }
}