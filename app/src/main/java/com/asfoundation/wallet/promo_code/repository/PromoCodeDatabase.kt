package com.asfoundation.wallet.promo_code.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [PromoCodeEntity::class], version = 2)
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
  }
}
