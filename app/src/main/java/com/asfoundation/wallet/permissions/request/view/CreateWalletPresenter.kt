package com.asfoundation.wallet.permissions.request.view


import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletCreatorInteract
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class CreateWalletPresenter(
        private val view: CreateWalletView,
        private val disposables: CompositeDisposable,
        private val interactor: WalletCreatorInteract,
        private val viewScheduler: Scheduler
) {
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
      view.getOnCreateWalletClick()
        .doOnNext { view.showLoading() }
        .flatMapSingle { interactor.create("Main Wallet") }
        .delay(1, TimeUnit.SECONDS)
        .observeOn(viewScheduler)
        .subscribe { view.showFinishAnimation() })
  }

  fun stop() = disposables.clear()
}
