package com.asfoundation.wallet.repository

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
class TransactionsDbModule {

  @Singleton
  @Provides
  fun provideTransactionsDatabase(@ApplicationContext context: Context): TransactionsDatabase {
    return Room.databaseBuilder(context.applicationContext, TransactionsDatabase::class.java,
        "transactions_database")
        .addMigrations(TransactionsDatabase.MIGRATION_1_2, TransactionsDatabase.MIGRATION_2_3,
            TransactionsDatabase.MIGRATION_3_4, TransactionsDatabase.MIGRATION_4_5,
            TransactionsDatabase.MIGRATION_5_6, TransactionsDatabase.MIGRATION_6_7,
            TransactionsDatabase.MIGRATION_7_8)
        .build()
  }

  @Singleton
  @Provides
  fun provideTransactionsDao(transactionsDatabase: TransactionsDatabase): TransactionsDao =
      transactionsDatabase.transactionsDao()

  @Singleton
  @Provides
  fun provideTransactionsLinkIdDao(
      transactionsDatabase: TransactionsDatabase): TransactionLinkIdDao =
      transactionsDatabase.transactionLinkIdDao()
}