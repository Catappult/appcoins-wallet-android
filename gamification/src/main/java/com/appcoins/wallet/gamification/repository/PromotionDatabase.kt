package com.appcoins.wallet.gamification.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.appcoins.wallet.gamification.repository.entity.PromotionEntity

@Database(entities = [PromotionEntity::class], version = 1)
@TypeConverters(PromotionTypeConverter::class)
abstract class PromotionDatabase : RoomDatabase() {

  abstract fun promotionDao(): PromotionDao

}