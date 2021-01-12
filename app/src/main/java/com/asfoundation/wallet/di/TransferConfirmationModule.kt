package com.asfoundation.wallet.di

import com.asfoundation.wallet.interact.FetchGasSettingsInteract
import com.asfoundation.wallet.interact.SendTransactionInteract
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.repository.GasPreferenceRepository
import com.asfoundation.wallet.router.GasSettingsRouter
import com.asfoundation.wallet.ui.TransferConfirmationInteractor
import com.asfoundation.wallet.viewmodel.TransferConfirmationViewModelFactory
import dagger.Module
import dagger.Provides

@Module(includes = [SendModule::class])
class TransferConfirmationModule {

  @Provides
  fun provideConfirmationViewModelFactory(interactor: TransferConfirmationInteractor,
                                          gasSettingsRouter: GasSettingsRouter,
                                          logger: Logger) =
      TransferConfirmationViewModelFactory(interactor, gasSettingsRouter, logger)

  @Provides
  fun providesConfirmationInteractor(sendTransactionInteract: SendTransactionInteract,
                                     gasSettingsInteract: FetchGasSettingsInteract,
                                     gasPreferenceRepository: GasPreferenceRepository): TransferConfirmationInteractor {
    return TransferConfirmationInteractor(sendTransactionInteract, gasSettingsInteract,
        gasPreferenceRepository)
  }
}