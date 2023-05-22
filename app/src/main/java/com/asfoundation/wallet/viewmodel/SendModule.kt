package com.asfoundation.wallet.di

import com.asfoundation.wallet.interact.FetchGasSettingsInteract
import com.asfoundation.wallet.router.TransactionsRouter
import com.asfoundation.wallet.router.TransferConfirmationRouter
import com.asfoundation.wallet.util.TransferParser
import com.asfoundation.wallet.viewmodel.SendViewModelFactory
import com.appcoins.wallet.feature.walletInfo.data.FindDefaultWalletInteract
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class SendModule {
  @Provides
  fun provideSendViewModelFactory(findDefaultWalletInteract: com.appcoins.wallet.feature.walletInfo.data.FindDefaultWalletInteract,
                                  transferConfirmationRouter: TransferConfirmationRouter,
                                  fetchGasSettingsInteract: FetchGasSettingsInteract,
                                  transferParser: TransferParser,
                                  transactionsRouter: TransactionsRouter): SendViewModelFactory {
    return SendViewModelFactory(findDefaultWalletInteract, fetchGasSettingsInteract,
      transferConfirmationRouter, transferParser, transactionsRouter)
  }
}