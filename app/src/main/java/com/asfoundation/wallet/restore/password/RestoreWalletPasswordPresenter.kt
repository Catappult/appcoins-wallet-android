package com.asfoundation.wallet.restore.password

import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.interact.WalletModel
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.RestoreErrorType
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class RestoreWalletPasswordPresenter(private val view: RestoreWalletPasswordView,
                                     private val data: RestoreWalletPasswordData,
                                     private val interactor: RestoreWalletPasswordInteractor,
                                     private val walletsEventSender: WalletsEventSender,
                                     private val currencyFormatUtils: CurrencyFormatUtils,
                                     private val disposable: CompositeDisposable,
                                     private val viewScheduler: Scheduler,
                                     private val networkScheduler: Scheduler,
                                     private val computationScheduler: Scheduler) {

  fun present() {
    val keystore = data.keystore
    populateUi(keystore)
    handleRestoreWalletButtonClicked(keystore)
  }

  private fun populateUi(keystore: String) {
    disposable.add(interactor.extractWalletAddress(keystore)
        .subscribeOn(networkScheduler)
        .flatMapObservable { address ->
          interactor.getOverallBalance(address)
              .observeOn(viewScheduler)
              .doOnNext { fiatValue ->
                view.updateUi(address, currencyFormatUtils.formatCurrency(fiatValue.amount),
                    fiatValue.symbol)
              }
        }
        .observeOn(viewScheduler)
        .subscribe({}, {
          it.printStackTrace()
          view.showError(RestoreErrorType.GENERIC)
        }))
  }

  private fun handleRestoreWalletButtonClicked(keystore: String) {
    disposable.add(view.restoreWalletButtonClick()
        .doOnNext {
          view.hideKeyboard()
          view.showWalletRestoreAnimation()
        }
        .doOnNext {
          walletsEventSender.sendWalletPasswordRestoreEvent(WalletsAnalytics.ACTION_IMPORT,
              WalletsAnalytics.STATUS_SUCCESS)
        }
        .doOnError {
          walletsEventSender.sendWalletPasswordRestoreEvent(WalletsAnalytics.ACTION_IMPORT,
              WalletsAnalytics.STATUS_FAIL, it.message)
        }
        .observeOn(computationScheduler)
        .flatMapSingle { interactor.restoreWallet(keystore, it) }
        .observeOn(viewScheduler)
        .doOnNext { handleWalletModel(it) }
        .doOnError {
          walletsEventSender.sendWalletCompleteRestoreEvent(WalletsAnalytics.STATUS_FAIL,
              it.message)
        }
        .subscribe({}, {
          it.printStackTrace()
          view.hideAnimation()
          view.showError(RestoreErrorType.GENERIC)
        }))
  }

  private fun setDefaultWallet(address: String) {
    disposable.add(interactor.setDefaultWallet(address)
        .doOnComplete { view.showWalletRestoredAnimation() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleWalletModel(walletModel: WalletModel) {
    if (walletModel.error.hasError) {
      view.hideAnimation()
      view.showError(walletModel.error.type)
      walletsEventSender.sendWalletCompleteRestoreEvent(WalletsAnalytics.STATUS_FAIL,
          walletModel.error.type.toString())
    } else {
      setDefaultWallet(walletModel.address)
      walletsEventSender.sendWalletCompleteRestoreEvent(WalletsAnalytics.STATUS_SUCCESS)
    }
  }

  fun stop() = disposable.clear()
}
