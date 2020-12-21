package com.asfoundation.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.router.GasSettingsRouter
import com.asfoundation.wallet.ui.ConfirmationInteractor

class ConfirmationViewModelFactory(private val confirmationInteractor: ConfirmationInteractor,
                                   private val gasSettingsRouter: GasSettingsRouter,
                                   private val logger: Logger) : ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return ConfirmationViewModel(confirmationInteractor, gasSettingsRouter, logger) as T
  }
}