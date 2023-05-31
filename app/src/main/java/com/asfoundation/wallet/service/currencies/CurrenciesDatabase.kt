package com.asfoundation.wallet.service.currencies

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.appcoins.wallet.feature.changecurrency.data.FiatCurrenciesDao
import com.appcoins.wallet.feature.changecurrency.data.FiatCurrencyEntity
import com.appcoins.wallet.feature.changecurrency.data.currencies.CurrencyConversionRateEntity
import com.appcoins.wallet.feature.changecurrency.data.currencies.CurrencyConversionRatesDao

@Database(entities = [CurrencyConversionRateEntity::class, FiatCurrencyEntity::class], version = 2)
abstract class CurrenciesDatabase : RoomDatabase() {

  abstract fun currencyConversionRatesDao(): CurrencyConversionRatesDao

  abstract fun fiatCurrenciesDao(): FiatCurrenciesDao

  companion object {
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
      override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS fiat_currencies (currency INTEGER PRIMARY KEY, flag TEXT , label TEXT , sign TEXT)")
      }
    }
  }
}