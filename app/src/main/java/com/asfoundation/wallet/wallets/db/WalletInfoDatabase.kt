package com.asfoundation.wallet.wallets.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.asfoundation.wallet.wallets.db.entity.WalletInfoEntity

@Database(
  entities = [WalletInfoEntity::class],
  version = 2
)
@TypeConverters(WalletInfoTypeConverter::class)
abstract class WalletInfoDatabase : RoomDatabase() {
  abstract fun walletInfoDao(): WalletInfoDao

  companion object {
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
      override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE WalletInfoEntity ADD COLUMN hasBackup INTEGER DEFAULT 0 NOT NULL")
      }
    }
  }

}