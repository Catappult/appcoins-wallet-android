package com.appcoins.wallet.feature.changecurrency.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface FiatCurrenciesDao {
  @Query("SELECT * FROM FiatCurrencyEntity WHERE currency = :currency LIMIT 1")
  fun getFiatCurrency(currency: String): FiatCurrencyEntity

  @Query("SELECT *  FROM FiatCurrencyEntity") fun getFiatCurrencies(): List<FiatCurrencyEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun saveCurrency(fiatCurrency: FiatCurrencyEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun saveAll(fiatCurrencyList: List<FiatCurrencyEntity>)

  @Query("DELETE FROM FiatCurrencyEntity") fun removeAll()

  @Transaction
  fun replaceAllBy(currencies: List<FiatCurrencyEntity>) {
    removeAll()
    saveAll(currencies)
  }
}
