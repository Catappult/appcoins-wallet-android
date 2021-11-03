package com.asfoundation.wallet.subscriptions.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [UserSubscriptionEntity::class], version = 1)
@TypeConverters(UserSubscriptionsConverter::class)
abstract class UserSubscriptionsDatabase : RoomDatabase() {

  abstract fun subscriptionsDao(): UserSubscriptionsDao
}
