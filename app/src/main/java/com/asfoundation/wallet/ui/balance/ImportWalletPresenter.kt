package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.interact.ImportWalletInteractor
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
                            private val importWalletInteractor: ImportWalletInteractor,
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
        .doOnNext { activityView.launchFileIntent(importWalletInteractor.getPath()) }
        .subscribe())
  }

  private fun handleFileChosen() {
    disposable.add(activityView.onFileChosen()
        .doOnNext { activityView.showWalletImportAnimation() }
        .flatMapSingle { importWalletInteractor.readFile(it) }
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
        .doOnNext {
          activityView.hideKeyboard()
          activityView.showWalletImportAnimation()
        }
        .observeOn(computationScheduler)
        .flatMapSingle { fetchWalletModel(it) }
        .observeOn(viewScheduler)
        .doOnNext { handleWalletModel(it) }
        .subscribe())
  }

  private fun setDefaultWallet(address: String) {
    disposable.add(importWalletInteractor.setDefaultWallet(address)
        .doOnComplete { activityView.showWalletImportedAnimation() }
        .subscribe())
  }

  private fun handleWalletModel(walletModel: WalletModel) {
    if (walletModel.error.hasError) {
      activityView.hideAnimation()
      if (walletModel.error.type == ImportErrorType.INVALID_PASS) {
        view.navigateToPasswordView(walletModel.keystore)
      } else view.showError(walletModel.error.type)
    } else {
      setDefaultWallet(walletModel.address)
    }
  }

  private fun fetchWalletModel(key: String): Single<WalletModel> {
    return if (importWalletInteractor.isKeystore(key)) importWalletInteractor.importKeystore(key)
    else {
      if (key.length == 64) importWalletInteractor.importPrivateKey(key)
      else Single.just(WalletModel(ImportError(ImportErrorType.INVALID_PRIVATE_KEY)))
    }
  }

  fun stop() {
    disposable.clear()
  }

}
