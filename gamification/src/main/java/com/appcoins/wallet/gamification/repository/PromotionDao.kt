package com.appcoins.wallet.gamification.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.appcoins.wallet.gamification.repository.entity.PromotionEntity
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface PromotionDao {

  @Query("select * from PromotionEntity")
  fun getAll(): Single<List<PromotionEntity>>

  @Query("DELETE FROM PromotionEntity")
  fun deleteAll(): Completable

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(promotions: List<PromotionEntity>): Completable

}
