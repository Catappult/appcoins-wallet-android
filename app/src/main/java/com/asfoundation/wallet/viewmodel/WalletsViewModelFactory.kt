package com.asfoundation.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import com.asfoundation.wallet.interact.*
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class WalletsViewModelFactory @Inject constructor(
    private val createWalletInteract: CreateWalletInteract,
    private val setDefaultWalletInteract: SetDefaultWalletInteract,
    private val fetchWalletsInteract: FetchWalletsInteract,
    private val findDefaultWalletInteract: FindDefaultWalletInteract,
    private val exportWalletInteract: ExportWalletInteract,
    private val logger: Logger,
    private val preferencesRepositoryType: PreferencesRepositoryType,
    private val compositeDisposable: CompositeDisposable) : Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return WalletsViewModel(createWalletInteract, setDefaultWalletInteract,
        fetchWalletsInteract, findDefaultWalletInteract, exportWalletInteract,
        logger, preferencesRepositoryType, compositeDisposable) as T
  }

}