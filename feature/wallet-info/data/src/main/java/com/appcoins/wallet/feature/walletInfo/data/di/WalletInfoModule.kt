package com.appcoins.wallet.feature.walletInfo.data.di

import com.appcoins.wallet.core.network.base.WalletRepository
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import com.appcoins.wallet.feature.walletInfo.data.WalletRepository as WalletRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface WalletInfoModule {

  @Binds
  fun bindWalletRepository(walletRepository: WalletRepositoryImpl) : WalletRepository

  @Binds
  fun bindWWalletRepositoryType(walletRepositoryType: WalletRepositoryImpl) : WalletRepositoryType
}
