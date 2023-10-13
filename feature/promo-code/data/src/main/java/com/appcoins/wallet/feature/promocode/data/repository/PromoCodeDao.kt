package com.appcoins.wallet.feature.promocode.data.repository

import androidx.room.*
import io.reactivex.Observable

@Dao
interface PromoCodeDao {

  @Query("SELECT *  FROM PromoCodeEntity limit 1")
  fun getSavedPromoCode(): Observable<List<PromoCodeEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun savePromoCode(promoCode: PromoCodeEntity)

  @Query("DELETE FROM PromoCodeEntity")
  fun removeSavedPromoCode()

  @Transaction
  fun replaceSavedPromoCodeBy(promoCode: PromoCodeEntity) {
    removeSavedPromoCode()
    savePromoCode(promoCode)
  }
}