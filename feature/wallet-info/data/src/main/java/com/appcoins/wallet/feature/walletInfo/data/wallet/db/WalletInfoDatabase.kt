package com.appcoins.wallet.feature.walletInfo.data.wallet.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.appcoins.wallet.feature.walletInfo.data.wallet.db.entity.WalletInfoEntity

@Database(
  entities = [WalletInfoEntity::class],
  version = 3
)
@TypeConverters(WalletInfoTypeConverter::class)
abstract class WalletInfoDatabase : RoomDatabase() {
  abstract fun walletInfoDao(): WalletInfoDao

  companion object {
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
      override fun migrate(database: SupportSQLiteDatabase) =
        database.execSQL("ALTER TABLE WalletInfoEntity ADD COLUMN hasBackup INTEGER DEFAULT 0 NOT NULL")
    }
    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
      override fun migrate(database: SupportSQLiteDatabase) =
        database.execSQL("ALTER TABLE WalletInfoEntity ADD COLUMN name TEXT")
    }
  }

}
