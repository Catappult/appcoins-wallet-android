package com.asfoundation.wallet.change_currency

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface FiatCurrenciesDao {
  @Query(
      "SELECT * FROM fiat_currencies WHERE currency = :currency LIMIT 1")
  fun getFiatCurrency(currency: String): Single<FiatCurrency>

  @Query("SELECT *  FROM fiat_currencies")
  fun getFiatCurrencies(): Single<List<FiatCurrency>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun saveCurrency(fiatCurrency: FiatCurrency) : Completable

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun saveAll(fiatCurrencyList: List<FiatCurrency>)

  @Query("DELETE FROM fiat_currencies")
  fun removeAll()
}