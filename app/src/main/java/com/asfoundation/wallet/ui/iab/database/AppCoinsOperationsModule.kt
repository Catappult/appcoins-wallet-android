package com.asfoundation.wallet.ui.iab.database

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
class AppCoinsOperationsModule {

  @Singleton
  @Provides
  fun provideAppCoinsOperationDatabase(@ApplicationContext context: Context): AppCoinsOperationDatabase {
    return Room.databaseBuilder(context, AppCoinsOperationDatabase::class.java, "appcoins_operations_database")
      .build()
  }

  @Singleton
  @Provides
  fun provideAppCoinsOperationDao(database: AppCoinsOperationDatabase): AppCoinsOperationDao {
    return database.appCoinsOperationDao()
  }
}