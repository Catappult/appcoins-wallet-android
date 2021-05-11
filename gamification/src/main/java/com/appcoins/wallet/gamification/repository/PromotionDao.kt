package com.appcoins.wallet.gamification.repository

import androidx.room.*
import com.appcoins.wallet.gamification.repository.entity.PromotionEntity
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface PromotionDao {

  @Query("select * from PromotionEntity")
  fun getPromotions(): Single<List<PromotionEntity>>

  @Transaction
  fun deleteAndInsert(promotions: List<PromotionEntity>) {
    // To ensure no concurrency issues occur (when trying to perform several deletions or
    // insertions), the deletion+insertion needs to be a single transaction in DB
    // This means that calling deletePromotions() or insertPromotions() separately should be avoided
    deletePromotions()
    insertPromotions(promotions)
  }

  @Query("DELETE FROM PromotionEntity")
  fun deletePromotions()

  @Insert(onConflict = OnConflictStrategy.ABORT)
  fun insertPromotions(promotions: List<PromotionEntity>)

}
