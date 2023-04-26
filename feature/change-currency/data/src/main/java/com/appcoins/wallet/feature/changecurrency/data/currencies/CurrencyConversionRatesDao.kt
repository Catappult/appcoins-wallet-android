package com.appcoins.wallet.feature.changecurrency.data.currencies

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.appcoins.wallet.feature.changecurrency.data.currencies.CurrencyConversionRateEntity.Companion.TABLE_NAME
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface CurrencyConversionRatesDao {

  @Query(
      "SELECT * from $TABLE_NAME WHERE currency_from = :currencyFrom LIMIT 1")
  fun getRate(currencyFrom: String): Single<CurrencyConversionRateEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertRate(rate: CurrencyConversionRateEntity): Completable

}