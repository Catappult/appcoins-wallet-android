package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.interact.WalletModel
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class ImportWalletPasswordPresenter(private val view: ImportWalletPasswordView,
                                    private val interactor: ImportWalletPasswordInteractor,
                                    private val disposable: CompositeDisposable,
                                    private val viewScheduler: Scheduler,
                                    private val networkScheduler: Scheduler,
                                    private val computationScheduler: Scheduler) {

  fun present(keystore: String) {
    populateUi(keystore)
    handleImportWalletButtonClicked(keystore)
  }

  private fun populateUi(keystore: String) {
    disposable.add(interactor.extractWalletAddress(keystore)
        .subscribeOn(networkScheduler)
        .flatMap { address ->
          interactor.getOverallBalance(address)
              .observeOn(viewScheduler)
              .doOnSuccess { fiatValue -> view.updateUi(address, fiatValue) }
        }
        .subscribe())
  }

  private fun handleImportWalletButtonClicked(keystore: String) {
    disposable.add(view.importWalletButtonClick()
        .observeOn(viewScheduler)
        .doOnNext { view.showWalletImportAnimation() }
        .observeOn(computationScheduler)
        .flatMapSingle { interactor.importWallet(keystore, it) }
        .observeOn(viewScheduler)
        .doOnNext { handleWalletModel(it) }
        .subscribe())
  }

  private fun setDefaultWallet(address: String) {
    disposable.add(interactor.setDefaultWallet(address)
        .observeOn(viewScheduler)
        .doOnComplete { view.showWalletImportedAnimation() }
        .subscribe())
  }

  private fun handleWalletModel(walletModel: WalletModel) {
    if (walletModel.error.hasError) {
      view.hideAnimation()
      view.showError(walletModel.error.type)
    } else {
      setDefaultWallet(walletModel.address)
    }
  }

  fun stop() {
    disposable.clear()
  }
}
