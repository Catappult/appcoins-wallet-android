package com.asfoundation.wallet.abtesting.db

import android.content.Context
import androidx.room.Room
import com.asfoundation.wallet.abtesting.ExperimentModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ABTestModule {

  @Singleton
  @Provides
  fun providesAbTestDatabase(@ApplicationContext context: Context): ABTestDatabase {
    return Room.databaseBuilder(context, ABTestDatabase::class.java, "abtest_database")
        .build()
  }

  @Singleton
  @Provides
  fun provideAbTestDao(abTestDatabase: ABTestDatabase): ExperimentDao =
      abTestDatabase.experimentDao()

  @Singleton
  @Provides
  @Named("ab-test-local-cache")
  fun providesAbTestLocalCache(): HashMap<String, ExperimentModel> {
    return HashMap()
  }
}