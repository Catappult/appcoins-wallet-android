package com.asfoundation.wallet.promo_code.repository

import androidx.room.*
import io.reactivex.Single

@Dao
interface PromoCodeDao {

  @Query("SELECT EXISTS(SELECT * FROM PromoCodeEntity)")
  fun hasPromoCode(): Single<Boolean>

  @Query("SELECT *  FROM PromoCodeEntity limit 1")
  fun getSavedPromoCode(): Single<PromoCodeEntity>

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