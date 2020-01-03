package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.interact.ImportWalletInteract
import com.asfoundation.wallet.interact.WalletModel
import com.asfoundation.wallet.util.ImportError
import com.asfoundation.wallet.util.ImportErrorType
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

class ImportWalletPresenter(private val view: ImportWalletView,
                            private val disposable: CompositeDisposable,
                            private val importWalletInteract: ImportWalletInteract,
                            private val viewScheduler: Scheduler,
                            private val computationScheduler: Scheduler) {

  fun present() {
    handleImportFromString()
  }

  private fun handleImportFromString() {
    disposable.add(view.importFromStringClick()
        .observeOn(viewScheduler)
        .doOnNext { view.showWalletImportAnimation() }
        .observeOn(computationScheduler)
        .flatMapSingle { fetchWalletModel(it) }
        .observeOn(viewScheduler)
        .doOnNext { handleWalletModel(it) }
        .subscribe())
  }

  private fun setDefaultWallet(address: String) {
    disposable.add(importWalletInteract.setDefaultWallet(address)
        .observeOn(viewScheduler)
        .doOnComplete { view.showWalletImportedAnimation() }
        .subscribe())
  }

  private fun handleWalletModel(walletModel: WalletModel) {
    if (walletModel.error.hasError) {
      view.hideAnimation()
      if (walletModel.error.type == ImportErrorType.INVALID_PASS) view.navigateToPasswordView()
      else view.showError(walletModel.error.type)
    } else {
      setDefaultWallet(walletModel.address)
    }
  }

  private fun fetchWalletModel(key: String): Single<WalletModel> {
    return if (importWalletInteract.isKeystore(key)) importWalletInteract.importKeystore(key)
    else {
      if (key.length == 64) importWalletInteract.importPrivateKey(key)
      else Single.just(WalletModel(ImportError(ImportErrorType.INVALID_PRIVATE_KEY)))
    }
  }

  fun stop() {
    disposable.clear()
  }

}
