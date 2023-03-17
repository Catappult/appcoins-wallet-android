package com.asfoundation.wallet.ui.wallets

import com.asfoundation.wallet.interact.DeleteWalletInteract
import com.appcoins.wallet.core.utils.jvm_common.Logger
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit

class WalletRemoveConfirmationPresenter(private val view: WalletRemoveConfirmationView,
                                        private val walletAddress: String,
                                        private val deleteWalletInteract: DeleteWalletInteract,
                                        private val logger: com.appcoins.wallet.core.utils.jvm_common.Logger,
                                        private val disposable: CompositeDisposable,
                                        private val viewScheduler: Scheduler,
                                        private val networkScheduler: Scheduler) {

  fun present() {
    handleNoButtonClick()
    handleYesButtonClick()
    handleAuthentication()
  }

  private fun handleNoButtonClick() {
    disposable.add(view.noButtonClick()
        .observeOn(viewScheduler)
        .doOnNext { view.navigateBack() }
        .subscribe())
  }

  private fun handleYesButtonClick() {
    disposable.add(view.yesButtonClick()
        .throttleFirst(100, TimeUnit.MILLISECONDS)
        .observeOn(viewScheduler)
        .flatMapSingle {
          var authenticationRequired = false
          if (deleteWalletInteract.hasAuthenticationPermission()) {
            view.showAuthentication()
            authenticationRequired = true
          } else {
            view.showRemoveWalletAnimation()
          }
          Single.just(authenticationRequired)
        }
        .filter { authenticationRequired -> !authenticationRequired }
        .observeOn(networkScheduler)
        .flatMapSingle { deleteWallet() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleAuthentication() {
    disposable.add(view.authenticationResult()
        .filter { it }
        .observeOn(viewScheduler)
        .doOnNext { view.showRemoveWalletAnimation() }
        .observeOn(networkScheduler)
        .flatMapSingle { deleteWallet() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun deleteWallet(): Single<Unit> {
    return Single.zip(deleteWalletInteract.delete(walletAddress)
        .toSingleDefault(Unit),
        Completable.timer(2, TimeUnit.SECONDS)
            .toSingleDefault(Unit),
        BiFunction { _: Unit, _: Unit -> })
        .observeOn(viewScheduler)
        .doOnSuccess { view.finish() }
        .doOnError {
          logger.log("WalletRemoveConfirmationPresenter", it)
          view.finish()
        }
  }

  fun stop() = disposable.clear()
}
