package com.asfoundation.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.router.GasSettingsRouter
import com.asfoundation.wallet.transfers.TransferConfirmationInteractor
import com.asfoundation.wallet.transfers.TransferConfirmationNavigator

class TransferConfirmationViewModelFactory(
    private val transferConfirmationInteractor: TransferConfirmationInteractor,
    private val gasSettingsRouter: GasSettingsRouter,
    private val logger: Logger,
    private val transferConfirmationNavigator: TransferConfirmationNavigator) :
    ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return TransferConfirmationViewModel(transferConfirmationInteractor, gasSettingsRouter,
        logger, transferConfirmationNavigator) as T
  }
}