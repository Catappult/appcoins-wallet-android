package com.asfoundation.wallet.feature_flags.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FeatureFlagsDao {

  @Query("SELECT * FROM feature_flag WHERE flagId = :flagId")
  suspend fun get(flagId: String): DBFeatureFlag?

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun save(flag: DBFeatureFlag)

  @Delete suspend fun remove(flag: DBFeatureFlag)
}
