package com.asfoundation.wallet.ui.wallets

import com.asfoundation.wallet.interact.DeleteWalletInteract
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit

class WalletRemoveConfirmationPresenter(private val view: WalletRemoveConfirmationView,
                                        private val walletAddress: String,
                                        private val deleteWalletInteract: DeleteWalletInteract,
                                        private val disposable: CompositeDisposable,
                                        private val viewScheduler: Scheduler,
                                        private val networkScheduler: Scheduler) {

  fun present() {
    handleNoButtonClick()
    handleYesButtonClick()
  }

  private fun handleNoButtonClick() {
    disposable.add(view.noButtonClick()
        .observeOn(viewScheduler)
        .doOnNext { view.navigateBack() }
        .subscribe())
  }

  private fun handleYesButtonClick() {
    disposable.add(view.yesButtonClick()
        .observeOn(viewScheduler)
        .doOnNext { view.showRemoveWalletAnimation() }
        .observeOn(networkScheduler)
        .flatMapSingle {
          Single.zip(deleteWalletInteract.delete(walletAddress).toSingleDefault(""),
              Completable.timer(2, TimeUnit.SECONDS).toSingleDefault(""),
              BiFunction { _: String, _: String -> })
        }
        .observeOn(viewScheduler)
        .doOnNext { view.finish() }
        .subscribe())
  }

  fun stop() {
    disposable.clear()
  }

}
