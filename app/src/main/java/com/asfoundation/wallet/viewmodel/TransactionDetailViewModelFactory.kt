package com.asfoundation.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.router.ExternalBrowserRouter
import com.asfoundation.wallet.router.TransactionDetailRouter
import com.asfoundation.wallet.support.SupportInteractor
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class TransactionDetailViewModelFactory(
    private val findDefaultNetworkInteract: FindDefaultNetworkInteract,
    private val findDefaultWalletInteract: FindDefaultWalletInteract,
    private val externalBrowserRouter: ExternalBrowserRouter,
    private val compositeDisposable: CompositeDisposable,
    private val supportInteractor: SupportInteractor,
    private val transactionDetailRouter: TransactionDetailRouter,
    private val viewScheduler: Scheduler
) : ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return TransactionDetailViewModel(findDefaultNetworkInteract, findDefaultWalletInteract,
        externalBrowserRouter, compositeDisposable, supportInteractor, transactionDetailRouter,
        viewScheduler) as T
  }

}