package com.appcoins.wallet.feature.walletInfo.data.di

import com.appcoins.wallet.core.network.base.IGetPrivateKeyUseCase
import com.appcoins.wallet.core.network.base.ISignUseCase
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.walletInfo.data.AccountKeystoreService
import com.appcoins.wallet.feature.walletInfo.data.Web3jKeystoreAccountService
import com.appcoins.wallet.feature.walletInfo.data.authentication.PasswordStore
import com.appcoins.wallet.feature.walletInfo.data.authentication.TrustPasswordStore
import com.appcoins.wallet.feature.walletInfo.data.wallet.AccountWalletService
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetPrivateKeyUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.SignUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WalletInfoBindingsModule {

  @Binds
  abstract fun bindAccountKeystoreService(impl: Web3jKeystoreAccountService): AccountKeystoreService

  @Binds
  abstract fun bindPasswordStore(impl: TrustPasswordStore): PasswordStore

  @Binds
  abstract fun bindWalletService(impl: AccountWalletService): WalletService

  @Binds
  @Singleton
  abstract fun bindGetPrivateKeyUseCase(impl: GetPrivateKeyUseCase): IGetPrivateKeyUseCase

  @Binds
  abstract fun bindSignUseCase(impl: SignUseCase): ISignUseCase
}