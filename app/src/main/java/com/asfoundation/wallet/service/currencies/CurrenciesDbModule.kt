package com.asfoundation.wallet.service.currencies

import android.content.Context
import androidx.room.Room
import com.appcoins.wallet.feature.changecurrency.data.FiatCurrenciesDao
import com.appcoins.wallet.feature.changecurrency.data.currencies.CurrencyConversionRatesPersistence
import com.appcoins.wallet.feature.changecurrency.data.currencies.RoomCurrencyConversionRatesPersistence
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class CurrenciesDbModule {

  @Singleton
  @Provides
  fun provideCurrencyConversionRatesDatabase(
      @ApplicationContext context: Context): CurrenciesDatabase {
    return Room.databaseBuilder(context, CurrenciesDatabase::class.java, "currencies_database")
        .addMigrations(
            CurrenciesDatabase.MIGRATION_1_2,
        )
        .build()
  }

  @Singleton
  @Provides
  fun provideRoomCurrencyConversionRatesPersistence(
      database: CurrenciesDatabase
  ): CurrencyConversionRatesPersistence {
    return RoomCurrencyConversionRatesPersistence(database.currencyConversionRatesDao())
  }

  @Singleton
  @Provides
  fun provideFiatCurrenciesDao(database: CurrenciesDatabase): FiatCurrenciesDao {
    return database.fiatCurrenciesDao()
  }
}