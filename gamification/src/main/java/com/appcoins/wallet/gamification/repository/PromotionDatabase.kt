package com.appcoins.wallet.gamification.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.appcoins.wallet.gamification.repository.entity.LevelEntity
import com.appcoins.wallet.gamification.repository.entity.LevelsEntity
import com.appcoins.wallet.gamification.repository.entity.PromotionEntity

@Database(entities = [PromotionEntity::class, LevelsEntity::class, LevelEntity::class], version = 2)
@TypeConverters(PromotionConverter::class)
abstract class PromotionDatabase : RoomDatabase() {

  companion object {
    //Adds the app name to the promotions entity object
    val MIGRATION_1_2: Migration = object : Migration(3, 4) {
      override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE PromotionEntity ADD COLUMN app_name TEXT")
      }
    }
  }

  abstract fun promotionDao(): PromotionDao
  abstract fun levelDao(): LevelDao
  abstract fun levelsDao(): LevelsDao

}