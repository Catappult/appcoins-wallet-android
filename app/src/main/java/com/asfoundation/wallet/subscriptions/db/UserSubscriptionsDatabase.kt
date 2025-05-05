package com.asfoundation.wallet.subscriptions.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [UserSubscriptionEntity::class], version = 2)
@TypeConverters(UserSubscriptionsConverter::class)
abstract class UserSubscriptionsDatabase : RoomDatabase() {

  abstract fun subscriptionsDao(): UserSubscriptionsDao
}

// Migration from schema version 1 to 2
val MIGRATION_1_2 = object : Migration(1, 2) {
  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL(
      """
      ALTER TABLE UserSubscriptionEntity 
      ADD COLUMN trialing INTEGER NOT NULL DEFAULT 0
      """.trimIndent()
    )
  }
}

