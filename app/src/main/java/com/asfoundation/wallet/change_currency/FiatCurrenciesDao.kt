package com.asfoundation.wallet.change_currency

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface FiatCurrenciesDao {
  @Query(
      "SELECT * FROM FiatCurrencyEntity WHERE currency = :currency LIMIT 1")
  fun getFiatCurrency(currency: String): Single<FiatCurrencyEntity>

  @Query("SELECT *  FROM FiatCurrencyEntity")
  fun getFiatCurrencies(): Single<List<FiatCurrencyEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun saveCurrency(fiatCurrency: FiatCurrencyEntity): Completable

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun saveAll(fiatCurrencyList: List<FiatCurrencyEntity>)

  @Query("DELETE FROM FiatCurrencyEntity")
  fun removeAll()

  @Transaction
  fun replaceAllBy(currencies: List<FiatCurrencyEntity>) {
    removeAll()
    saveAll(currencies)
  }
}