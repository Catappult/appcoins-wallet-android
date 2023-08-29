package com.appcoins.wallet.feature.walletInfo.data.wallet.db

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
class WalletInfoModule {

  @Singleton
  @Provides
  fun providesWalletInfoDatabase(@ApplicationContext context: Context) =
    Room.databaseBuilder(context, WalletInfoDatabase::class.java, "wallet_info_database")
      .addMigrations(WalletInfoDatabase.MIGRATION_1_2)
      .addMigrations(WalletInfoDatabase.MIGRATION_2_3)
      .build()

  @Singleton
  @Provides
  fun providesWalletInfoDao(walletInfoDatabase: WalletInfoDatabase) =
    walletInfoDatabase.walletInfoDao()
}