package com.asfoundation.wallet.feature_flags.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DBFeatureFlag::class], version = 1)
abstract class FeatureFlagsDatabase : RoomDatabase() {

  abstract fun getFeatureFlagsDao(): FeatureFlagsDao
}