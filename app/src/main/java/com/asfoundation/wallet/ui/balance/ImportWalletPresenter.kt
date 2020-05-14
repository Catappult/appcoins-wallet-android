package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.interact.ImportWalletInteract
import com.asfoundation.wallet.interact.WalletModel
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.util.ImportError
import com.asfoundation.wallet.util.ImportErrorType
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

class ImportWalletPresenter(private val view: ImportWalletView,
                            private val activityView: ImportWalletActivityView,
                            private val disposable: CompositeDisposable,
                            private val importWalletInteract: ImportWalletInteract,
                            private val logger: Logger,
                            private val viewScheduler: Scheduler,
                            private val computationScheduler: Scheduler) {

  fun present() {
    handleImportFromString()
    handleImportFromFile()
    handleFileChosen()
    handleOnPermissionsGiven()
  }

  private fun handleOnPermissionsGiven() {
    disposable.add(activityView.onPermissionsGiven()
        .doOnNext { activityView.launchFileIntent(importWalletInteract.getPath()) }
        .subscribe())
  }

  private fun handleFileChosen() {
    disposable.add(activityView.onFileChosen()
        .doOnNext { activityView.showWalletImportAnimation() }
        .flatMapSingle { importWalletInteract.readFile(it) }
        .observeOn(computationScheduler)
        .flatMapSingle { fetchWalletModel(it) }
        .observeOn(viewScheduler)
        .doOnNext { handleWalletModel(it) }
        .subscribe({}, {
          logger.log("ImportWalletPresenter", it)
          activityView.hideAnimation()
          view.showError(ImportErrorType.INVALID_KEYSTORE)
        })
    )
  }

  private fun handleImportFromFile() {
    disposable.add(view.importFromFileClick()
        .doOnNext { activityView.askForReadPermissions() }
        .subscribe())
  }

  private fun handleImportFromString() {
    disposable.add(view.importFromStringClick()
        .doOnNext { activityView.showWalletImportAnimation() }
        .observeOn(computationScheduler)
        .flatMapSingle { fetchWalletModel(it) }
        .observeOn(viewScheduler)
        .doOnNext { handleWalletModel(it) }
        .subscribe())
  }

  private fun setDefaultWallet(address: String) {
    disposable.add(importWalletInteract.setDefaultWallet(address)
        .doOnComplete { activityView.showWalletImportedAnimation() }
        .subscribe())
  }

  private fun handleWalletModel(walletModel: WalletModel) {
    if (walletModel.error.hasError) {
      activityView.hideAnimation()
      if (walletModel.error.type == ImportErrorType.INVALID_PASS) view.navigateToPasswordView(
          walletModel.keystore)
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
