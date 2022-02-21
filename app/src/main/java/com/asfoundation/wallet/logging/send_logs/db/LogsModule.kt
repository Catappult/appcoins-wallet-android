package com.asfoundation.wallet.logging.send_logs.db

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
class LogsModule {

  @Singleton
  @Provides
  fun provideLogsDatabase(@ApplicationContext context: Context): LogsDatabase {
    return Room.databaseBuilder(context, LogsDatabase::class.java, "logs_database")
        .build()
  }

  @Singleton
  @Provides
  fun provideLogsDao(database: LogsDatabase): LogsDao {
    return database.logsDao()
  }
}