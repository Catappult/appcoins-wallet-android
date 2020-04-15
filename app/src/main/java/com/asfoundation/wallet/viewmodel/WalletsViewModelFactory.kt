package com.asfoundation.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import com.asfoundation.wallet.interact.*
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.router.ImportWalletRouter
import com.asfoundation.wallet.router.TransactionsRouter
import javax.inject.Inject

class WalletsViewModelFactory @Inject constructor(
    private val createWalletInteract: CreateWalletInteract,
    private val setDefaultWalletInteract: SetDefaultWalletInteract,
    private val deleteWalletInteract: DeleteWalletInteract,
    private val fetchWalletsInteract: FetchWalletsInteract,
    private val findDefaultWalletInteract: FindDefaultWalletInteract,
    private val exportWalletInteract: ExportWalletInteract,
    private val importWalletRouter: ImportWalletRouter,
    private val transactionsRouter: TransactionsRouter,
    private val logger: Logger,
    private val preferencesRepositoryType: PreferencesRepositoryType) : Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return WalletsViewModel(createWalletInteract, setDefaultWalletInteract,
        deleteWalletInteract, fetchWalletsInteract, findDefaultWalletInteract, exportWalletInteract,
        importWalletRouter, transactionsRouter, logger, preferencesRepositoryType) as T
  }

}