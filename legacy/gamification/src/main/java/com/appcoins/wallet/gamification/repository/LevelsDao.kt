package com.appcoins.wallet.gamification.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.appcoins.wallet.gamification.repository.entity.LevelsEntity
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface LevelsDao {

  @Query("select * from LevelsEntity")
  fun getLevels(): Single<LevelsEntity>

  @Query("DELETE FROM LevelsEntity")
  fun deleteLevels(): Completable

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertLevels(promotions: LevelsEntity): Completable

}
