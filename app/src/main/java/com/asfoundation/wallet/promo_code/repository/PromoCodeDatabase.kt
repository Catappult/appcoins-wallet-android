package com.asfoundation.wallet.promo_code.repository

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PromoCodeEntity::class], version = 1)
abstract class PromoCodeDatabase : RoomDatabase() {

  abstract fun promoCodeDao(): PromoCodeDao
}
