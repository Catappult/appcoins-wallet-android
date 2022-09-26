package com.asfoundation.wallet.feature_flags.db

import androidx.room.*

@Dao
interface FeatureFlagsDao {

  @Query("SELECT * FROM feature_flag WHERE flagId = :flagId")
  suspend fun get(flagId: String): DBFeatureFlag?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun save(flag: DBFeatureFlag)

  @Delete
  suspend fun remove(flag: DBFeatureFlag)
}
