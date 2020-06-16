package com.asfoundation.wallet.di

import com.asfoundation.wallet.interact.FetchGasSettingsInteract
import com.asfoundation.wallet.interact.SendTransactionInteract
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.router.GasSettingsRouter
import com.asfoundation.wallet.viewmodel.ConfirmationViewModelFactory
import dagger.Module
import dagger.Provides

@Module(includes = [SendModule::class])
class ConfirmationModule {

  @Provides
  fun provideConfirmationViewModelFactory(sendTransactionInteract: SendTransactionInteract,
                                          gasSettingsRouter: GasSettingsRouter,
                                          gasSettingsInteract: FetchGasSettingsInteract,
                                          logger: Logger) =
      ConfirmationViewModelFactory(sendTransactionInteract, gasSettingsRouter, gasSettingsInteract,
          logger)

}