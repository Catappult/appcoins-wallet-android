package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.interact.WalletModel
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class RestoreWalletPasswordPresenter(private val view: RestoreWalletPasswordView,
                                     private val activityView: RestoreWalletActivityView,
                                     private val interactor: RestoreWalletPasswordInteractor,
                                     private val disposable: CompositeDisposable,
                                     private val viewScheduler: Scheduler,
                                     private val networkScheduler: Scheduler,
                                     private val computationScheduler: Scheduler) {

  fun present(keystore: String) {
    populateUi(keystore)
    handleRestoreWalletButtonClicked(keystore)
  }

  private fun populateUi(keystore: String) {
    disposable.add(interactor.extractWalletAddress(keystore)
        .subscribeOn(networkScheduler)
        .flatMapObservable { address ->
          interactor.getOverallBalance(address)
              .observeOn(viewScheduler)
              .doOnNext { fiatValue -> view.updateUi(address, fiatValue) }
        }
        .subscribe())
  }

  private fun handleRestoreWalletButtonClicked(keystore: String) {
    disposable.add(view.restoreWalletButtonClick()
        .doOnNext {
          activityView.hideKeyboard()
          view.showWalletRestoreAnimation()
        }
        .observeOn(computationScheduler)
        .flatMapSingle { interactor.restoreWallet(keystore, it) }
        .observeOn(viewScheduler)
        .doOnNext { handleWalletModel(it) }
        .subscribe())
  }

  private fun setDefaultWallet(address: String) {
    disposable.add(interactor.setDefaultWallet(address)
        .doOnComplete { view.showWalletRestoredAnimation() }
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

  fun stop() = disposable.clear()
}
