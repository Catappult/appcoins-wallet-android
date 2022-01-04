package com.asfoundation.wallet.abtesting.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ExperimentEntity::class], version = 1)
abstract class ABTestDatabase : RoomDatabase() {

  abstract fun experimentDao(): ExperimentDao
}