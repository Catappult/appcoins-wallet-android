package com.appcoins.wallet.feature.promocode.data.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [PromoCodeEntity::class], version = 3)
abstract class PromoCodeDatabase : RoomDatabase() {

  abstract fun promoCodeDao(): PromoCodeDao

  companion object {
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
      override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE PromoCodeEntity ADD COLUMN appName TEXT")
        database.execSQL("ALTER TABLE PromoCodeEntity ADD COLUMN appPackageName TEXT")
        database.execSQL("ALTER TABLE PromoCodeEntity ADD COLUMN appIcon TEXT")
      }
    }

    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
      override fun migrate(database: SupportSQLiteDatabase) {
        // to remove expiry_date and expired fields
        // add validity field
        database.execSQL("CREATE TABLE PromoCodeEntity_new (code TEXT NOT NULL, bonus REAL, validityState INTEGER, appName TEXT, appPackageName TEXT, appIcon TEXT, PRIMARY KEY(code))")
        database.execSQL("INSERT INTO PromoCodeEntity_new (code, bonus, validityState, appName, appPackageName, appIcon) SELECT code, bonus, expired, appName, appPackageName, appIcon FROM PromoCodeEntity")
        database.execSQL("DROP TABLE PromoCodeEntity")
        database.execSQL("ALTER TABLE PromoCodeEntity_new RENAME TO PromoCodeEntity")
      }
    }
  }
}
