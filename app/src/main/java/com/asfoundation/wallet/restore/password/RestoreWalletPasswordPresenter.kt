package com.asfoundation.wallet.restore.password

import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.RestoreErrorType
import com.asfoundation.wallet.wallets.WalletModel
import com.asfoundation.wallet.wallets.usecases.ObserveWalletInfoUseCase
import io.reactivex.disposables.CompositeDisposable

class RestoreWalletPasswordPresenter(private val view: RestoreWalletPasswordView,
                                     private val data: RestoreWalletPasswordData,
                                     private val observeWalletInfoUseCase: ObserveWalletInfoUseCase,
                                     private val interactor: RestoreWalletPasswordInteractor,
                                     private val walletsEventSender: WalletsEventSender,
                                     private val currencyFormatUtils: CurrencyFormatUtils,
                                     private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
                                     private val disposable: CompositeDisposable,
                                     private val rxSchedulers: RxSchedulers) {

  fun present() {
    val keystore = data.keystore
    populateUi(keystore)
//    handleRestoreWalletButtonClicked(keystore)
  }

  private fun populateUi(keystore: String) {
    disposable.add(interactor.extractWalletAddress(keystore)
        .subscribeOn(rxSchedulers.io)
        .flatMapObservable { address ->
          observeWalletInfoUseCase(address, update = true, updateFiat = true)
              .observeOn(rxSchedulers.main)
              .doOnNext { walletInfo ->
                val overallFiat = walletInfo.walletBalance.overallFiat
                view.updateUi(address, currencyFormatUtils.formatCurrency(overallFiat.amount),
                    overallFiat.symbol)
              }
        }
        .observeOn(rxSchedulers.main)
        .subscribe({}, {
          it.printStackTrace()
          view.showError(RestoreErrorType.GENERIC)
        }))
  }

//  private fun handleRestoreWalletButtonClicked(keystore: String) {
//    disposable.add(view.restoreWalletButtonClick()
//        .doOnNext {
//          view.hideKeyboard()
//          view.showWalletRestoreAnimation()
//        }
//        .doOnNext {
//          walletsEventSender.sendWalletPasswordRestoreEvent(WalletsAnalytics.ACTION_IMPORT,
//              WalletsAnalytics.STATUS_SUCCESS)
//        }
//        .doOnError {
//          walletsEventSender.sendWalletPasswordRestoreEvent(WalletsAnalytics.ACTION_IMPORT,
//              WalletsAnalytics.STATUS_FAIL, it.message)
//        }
//        .observeOn(computationScheduler)
//        .flatMapSingle { interactor.restoreWallet(keystore, it) }
//        .observeOn(viewScheduler)
//        .doOnNext { handleWalletModel(it) }
//        .doOnError {
//          walletsEventSender.sendWalletCompleteRestoreEvent(WalletsAnalytics.STATUS_FAIL,
//              it.message)
//        }
//        .subscribe({}, {
//          it.printStackTrace()
//          view.hideAnimation()
//          view.showError(RestoreErrorType.GENERIC)
//        }))
//  }

  private fun setDefaultWallet(address: String) {
    disposable.add(interactor.setDefaultWallet(address)
        .doOnComplete {
          setOnboardingCompletedUseCase()
          view.showWalletRestoredAnimation()
        }
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
