package com.appcoins.wallet.feature.promocode.data.repository

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
class PromoCodeModule {

  @Singleton
  @Provides
  fun providePromoCodeDatabase(@ApplicationContext context: Context): PromoCodeDatabase {
    return Room.databaseBuilder(context, PromoCodeDatabase::class.java, "promo_code_database")
      .addMigrations(PromoCodeDatabase.MIGRATION_1_2)
      .addMigrations(PromoCodeDatabase.MIGRATION_2_3)
      .build()
  }

  @Singleton
  @Provides
  fun providePromoCodeDao(database: PromoCodeDatabase): PromoCodeDao {
    return database.promoCodeDao()
  }
}