package com.appcoins.wallet.gamification.repository

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class PromotionsDbModule {

  @Singleton
  @Provides
  fun providesPromotionDatabase(@ApplicationContext context: Context): PromotionDatabase {
    return Room.databaseBuilder(context, PromotionDatabase::class.java, "promotion_database")
      .addMigrations(PromotionDatabase.MIGRATION_1_2)
      .addMigrations(PromotionDatabase.MIGRATION_2_3)
      .addMigrations(PromotionDatabase.MIGRATION_3_4)
      .addMigrations(PromotionDatabase.MIGRATION_4_5)
      .addMigrations(PromotionDatabase.MIGRATION_5_6)
      .addMigrations(PromotionDatabase.MIGRATION_6_7)
      .addMigrations(PromotionDatabase.MIGRATION_7_8)
      .addMigrations(PromotionDatabase.MIGRATION_8_9)
      .addMigrations(PromotionDatabase.MIGRATION_9_10)
      .build()
  }

  @Singleton
  @Provides
  fun providesPromotionDao(promotionDatabase: PromotionDatabase) = promotionDatabase.promotionDao()

  @Singleton
  @Provides
  fun providesLevelsDao(promotionDatabase: PromotionDatabase) = promotionDatabase.levelsDao()

  @Singleton
  @Provides
  fun providesLevelDao(promotionDatabase: PromotionDatabase) = promotionDatabase.levelDao()

  @Singleton
  @Provides
  fun providesWalletOriginDao(promotionDatabase: PromotionDatabase): WalletOriginDao =
    promotionDatabase.walletOriginDao()
}