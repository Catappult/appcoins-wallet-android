package com.asfoundation.wallet.wallets.db

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
  fun providesWalletInfoDatabase(@ApplicationContext context: Context): WalletInfoDatabase {
    return Room.databaseBuilder(context, WalletInfoDatabase::class.java, "wallet_info_database")
      .build()
  }

  @Singleton
  @Provides
  fun providesWalletInfoDao(walletInfoDatabase: WalletInfoDatabase): WalletInfoDao {
    return walletInfoDatabase.walletInfoDao()
  }
}