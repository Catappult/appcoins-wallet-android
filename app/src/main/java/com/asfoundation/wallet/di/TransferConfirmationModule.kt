package com.asfoundation.wallet.di

import com.asfoundation.wallet.interact.FetchGasSettingsInteract
import com.asfoundation.wallet.interact.SendTransactionInteract
import com.appcoins.wallet.commons.Logger
import com.asfoundation.wallet.repository.GasPreferenceRepository
import com.asfoundation.wallet.router.GasSettingsRouter
import com.asfoundation.wallet.transfers.TransferConfirmationActivity
import com.asfoundation.wallet.transfers.TransferConfirmationInteractor
import com.asfoundation.wallet.transfers.TransferConfirmationNavigator
import com.asfoundation.wallet.viewmodel.TransferConfirmationViewModelFactory
import dagger.Module
import dagger.Provides

@Module(includes = [SendModule::class])
class TransferConfirmationModule {

  @Provides
  fun provideConfirmationViewModelFactory(interactor: TransferConfirmationInteractor,
                                          gasSettingsRouter: GasSettingsRouter,
                                          logger: Logger,
                                          navigator: TransferConfirmationNavigator) =
      TransferConfirmationViewModelFactory(interactor, gasSettingsRouter, logger, navigator)

  @Provides
  fun providesConfirmationInteractor(sendTransactionInteract: SendTransactionInteract,
                                     gasSettingsInteract: FetchGasSettingsInteract,
                                     gasPreferenceRepository: GasPreferenceRepository): TransferConfirmationInteractor {
    return TransferConfirmationInteractor(sendTransactionInteract, gasSettingsInteract,
        gasPreferenceRepository)
  }

  @Provides
  fun providesConfirmationNavigator(
      transferConfirmationActivity: TransferConfirmationActivity): TransferConfirmationNavigator {
    return TransferConfirmationNavigator(transferConfirmationActivity.supportFragmentManager)
  }
}