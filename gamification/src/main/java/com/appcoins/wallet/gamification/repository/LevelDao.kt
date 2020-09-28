package com.appcoins.wallet.gamification.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.appcoins.wallet.gamification.repository.entity.LevelEntity
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface LevelDao {

  @Query("select * from LevelEntity")
  fun getLevels(): Single<List<LevelEntity>>

  @Query("DELETE FROM LevelEntity")
  fun deleteLevels(): Completable

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertLevels(levels: List<LevelEntity>): Completable

}
