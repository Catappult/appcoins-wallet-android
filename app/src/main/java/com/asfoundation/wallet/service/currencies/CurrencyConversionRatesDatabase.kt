package com.asfoundation.wallet.service.currencies

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CurrencyConversionRateEntity::class], version = 1)
abstract class CurrencyConversionRatesDatabase : RoomDatabase() {

  abstract fun currencyConversionRatesDao(): CurrencyConversionRatesDao
}