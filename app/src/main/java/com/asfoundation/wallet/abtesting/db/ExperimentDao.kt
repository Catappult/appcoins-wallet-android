package com.asfoundation.wallet.abtesting.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Single

@Dao
interface ExperimentDao {

  @Query("SELECT * FROM experiment WHERE experimentName = :identifier")
  operator fun get(identifier: String?): Single<ExperimentEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun save(experiment: ExperimentEntity)
}
