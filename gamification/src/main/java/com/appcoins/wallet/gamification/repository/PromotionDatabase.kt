package com.appcoins.wallet.gamification.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.appcoins.wallet.gamification.repository.entity.LevelEntity
import com.appcoins.wallet.gamification.repository.entity.LevelsEntity
import com.appcoins.wallet.gamification.repository.entity.PromotionEntity
import com.appcoins.wallet.gamification.repository.entity.WalletOriginEntity

@Database(
    entities = [PromotionEntity::class, LevelsEntity::class, LevelEntity::class, WalletOriginEntity::class],
    version = 3)
@TypeConverters(PromotionConverter::class)
abstract class PromotionDatabase : RoomDatabase() {

  companion object {
    //Adds the app name to the promotions entity object
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
      override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE PromotionEntity ADD COLUMN details_link TEXT")
      }
    }

    //Adds the the wallet origin table
    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
      override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS WalletOriginEntity (wallet_address TEXT PRIMARY KEY NOT NULL, wallet_origin TEXT NOT NULL)")
      }
    }
  }

  abstract fun promotionDao(): PromotionDao
  abstract fun levelDao(): LevelDao
  abstract fun levelsDao(): LevelsDao
  abstract fun walletOriginDao(): WalletOriginDao

}