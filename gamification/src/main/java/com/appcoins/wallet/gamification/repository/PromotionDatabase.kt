package com.appcoins.wallet.gamification.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.appcoins.wallet.gamification.repository.entity.LevelEntity
import com.appcoins.wallet.gamification.repository.entity.LevelsEntity
import com.appcoins.wallet.gamification.repository.entity.PromotionEntity

@Database(entities = [PromotionEntity::class, LevelsEntity::class, LevelEntity::class], version = 1)
@TypeConverters(PromotionConverter::class)
abstract class PromotionDatabase : RoomDatabase() {

  abstract fun promotionDao(): PromotionDao
  abstract fun levelDao(): LevelDao
  abstract fun levelsDao(): LevelsDao

}