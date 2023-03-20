package com.asfoundation.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asfoundation.wallet.router.GasSettingsRouter
import com.asfoundation.wallet.transfers.TransferConfirmationInteractor
import com.asfoundation.wallet.transfers.TransferConfirmationNavigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@InstallIn(ActivityComponent::class)
@Module
class TransferConfirmationModule {

  @Provides
  fun provideConfirmationViewModelFactory(
    interactor: TransferConfirmationInteractor,
    gasSettingsRouter: GasSettingsRouter,
    logger: Logger,
    navigator: TransferConfirmationNavigator) =
    TransferConfirmationViewModelFactory(interactor, gasSettingsRouter, logger, navigator)
}

class TransferConfirmationViewModelFactory(
  private val transferConfirmationInteractor: TransferConfirmationInteractor,
  private val gasSettingsRouter: GasSettingsRouter,
  private val logger: Logger,
  private val transferConfirmationNavigator: TransferConfirmationNavigator) :
  ViewModelProvider.Factory {

  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return TransferConfirmationViewModel(transferConfirmationInteractor, gasSettingsRouter,
      logger, transferConfirmationNavigator) as T
  }
}