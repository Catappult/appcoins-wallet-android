package com.asfoundation.wallet.viewmodel

import com.appcoins.wallet.feature.walletInfo.data.wallet.FindDefaultWalletInteract
import com.asfoundation.wallet.interact.FetchGasSettingsInteract
import com.asfoundation.wallet.router.TransferConfirmationRouter
import com.asfoundation.wallet.util.TransferParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class SendModule {
  @Provides
  fun provideSendViewModelFactory(
    findDefaultWalletInteract: FindDefaultWalletInteract,
    transferConfirmationRouter: TransferConfirmationRouter,
    fetchGasSettingsInteract: FetchGasSettingsInteract,
    transferParser: TransferParser,
  ): SendViewModelFactory {
    return SendViewModelFactory(
      findDefaultWalletInteract,
      fetchGasSettingsInteract,
      transferConfirmationRouter,
      transferParser
    )
  }
}