package com.asfoundation.wallet.permissions

import com.asfoundation.wallet.interact.CreateWalletInteract
import io.reactivex.disposables.CompositeDisposable

class CreateWalletPresenter(private val view: CreateWalletView,
                            private val disposables: CompositeDisposable,
                            private val interactor: CreateWalletInteract) {
  fun present() {
    handleOnCreateWalletClick()
    handleOnCancelClick()
  }

  private fun handleOnCancelClick() {
    disposables.add(view.getCancelClick().subscribe { view.closeCancel() })
  }

  private fun handleOnCreateWalletClick() {
    disposables.add(
        view.getOnCreateWalletClick().flatMapSingle { interactor.create() }.subscribe { view.closeSuccess() })
  }

  fun stop() {
    disposables.clear()
  }
}
