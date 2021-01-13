package com.asfoundation.wallet.di

import com.asfoundation.wallet.interact.FetchGasSettingsInteract
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.router.TransactionsRouter
import com.asfoundation.wallet.router.TransferConfirmationRouter
import com.asfoundation.wallet.util.TransferParser
import com.asfoundation.wallet.viewmodel.SendViewModelFactory
import dagger.Module
import dagger.Provides
import io.reactivex.subjects.PublishSubject

@Module
class SendModule {
  @Provides
  fun provideSendViewModelFactory(findDefaultWalletInteract: FindDefaultWalletInteract,
                                  transferConfirmationRouter: TransferConfirmationRouter,
                                  fetchGasSettingsInteract: FetchGasSettingsInteract,
                                  transferParser: TransferParser,
                                  transactionsRouter: TransactionsRouter): SendViewModelFactory {
    return SendViewModelFactory(findDefaultWalletInteract, fetchGasSettingsInteract,
        transferConfirmationRouter, transferParser, transactionsRouter)
  }

  @Provides
  fun provideConfirmationRouter() =
      TransferConfirmationRouter(
          PublishSubject.create())

  @Provides
  fun provideTransactionsRouter() = TransactionsRouter()
}