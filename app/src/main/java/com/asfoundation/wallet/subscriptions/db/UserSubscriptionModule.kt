package com.asfoundation.wallet.subscriptions.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class UserSubscriptionModule {

  @Singleton
  @Provides
  fun providesUserSubscriptionsDatabase(
      @ApplicationContext context: Context): UserSubscriptionsDatabase {
    return Room.databaseBuilder(context, UserSubscriptionsDatabase::class.java,
        "user_subscription_database")
        .build()
  }

  @Singleton
  @Provides
  fun providesUserSubscriptionDao(
      userSubscriptionsDatabase: UserSubscriptionsDatabase): UserSubscriptionsDao {
    return userSubscriptionsDatabase.subscriptionsDao()
  }

}