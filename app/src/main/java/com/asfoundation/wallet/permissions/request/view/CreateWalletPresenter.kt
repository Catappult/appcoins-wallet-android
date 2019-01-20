package com.asfoundation.wallet.permissions.request.view

import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.interact.CreateWalletInteract
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit

class CreateWalletPresenter(private val view: CreateWalletView,
                            private val disposables: CompositeDisposable,
                            private val interactor: CreateWalletInteract,
                            private val viewScheduler: Scheduler) {
  fun present() {
    handleOnCreateWalletClick()
    handleOnCancelClick()
    handleOnFinishAnimationFinish()
  }

  private fun handleOnFinishAnimationFinish() {
    disposables.add(view.getFinishAnimationFinishEvent().subscribe { view.closeSuccess() })
  }

  private fun handleOnCancelClick() {
    disposables.add(view.getCancelClick().subscribe { view.closeCancel() })
  }

  private fun handleOnCreateWalletClick() {
    disposables.add(
        view.getOnCreateWalletClick().doOnNext { view.showLoading() }
            .flatMapSingle {
              Single.zip(interactor.create(), Single.timer(1, TimeUnit.SECONDS),
                  BiFunction { wallet: Wallet, _: Long -> wallet })
            }
            .observeOn(viewScheduler)
            .subscribe { view.showFinishAnimation() })
  }

  fun stop() {
    disposables.clear()
  }
}
