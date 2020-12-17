package com.asfoundation.wallet.di

import com.asfoundation.wallet.interact.FetchGasSettingsInteract
import com.asfoundation.wallet.interact.SendTransactionInteract
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.repository.GasPreferenceRepository
import com.asfoundation.wallet.router.GasSettingsRouter
import com.asfoundation.wallet.ui.ConfirmationInteractor
import com.asfoundation.wallet.viewmodel.ConfirmationViewModelFactory
import dagger.Module
import dagger.Provides

@Module(includes = [SendModule::class])
class ConfirmationModule {

  @Provides
  fun provideConfirmationViewModelFactory(confirmationInteractor: ConfirmationInteractor,
                                          gasSettingsRouter: GasSettingsRouter,
                                          logger: Logger) =
      ConfirmationViewModelFactory(confirmationInteractor, gasSettingsRouter, logger)

  @Provides
  fun providesConfirmationInteractor(sendTransactionInteract: SendTransactionInteract,
                                     gasSettingsInteract: FetchGasSettingsInteract,
                                     gasPreferenceRepository: GasPreferenceRepository): ConfirmationInteractor {
    return ConfirmationInteractor(sendTransactionInteract, gasSettingsInteract,
        gasPreferenceRepository)
  }
}