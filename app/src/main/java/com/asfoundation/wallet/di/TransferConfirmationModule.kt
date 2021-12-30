package com.asfoundation.wallet.di

import androidx.appcompat.app.AppCompatActivity
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
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@InstallIn(ActivityComponent::class)
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
      activity: AppCompatActivity): TransferConfirmationNavigator {
    return TransferConfirmationNavigator(activity.supportFragmentManager)
  }
}