package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.wallets.FindDefaultWalletInteract
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class QrCodePresenter(
    val view: QrCodeView,
    val findDefaultWalletInteract: FindDefaultWalletInteract,
    val disposable: CompositeDisposable,
    val viewScheduler: Scheduler) {

  fun present() {
    requestActiveWalletAddress()
    handleCloseClick()
    handleCopyClick()
    handleShareClick()
  }

  private fun requestActiveWalletAddress() {
    disposable.add(
        findDefaultWalletInteract.find()
            .observeOn(viewScheduler)
            .doOnSuccess {
              view.setWalletAddress(it.address)
              view.createQrCode(it.address)
            }
            .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleCopyClick() {
    disposable.add(
        view.copyClick()
            .flatMapSingle { findDefaultWalletInteract.find() }
            .observeOn(viewScheduler)
            .doOnNext { view.setAddressToClipBoard(it.address) }
            .subscribe())
  }

  private fun handleShareClick() {
    disposable.add(
        view.shareClick()
            .flatMapSingle { findDefaultWalletInteract.find() }
            .observeOn(viewScheduler)
            .doOnNext { view.showShare(it.address) }
            .subscribe())

  }

  private fun handleCloseClick() {
    disposable.add(
        view.closeClick()
            .observeOn(viewScheduler)
            .doOnNext { view.closeSuccess() }
            .subscribe())
  }

  fun stop() {
    disposable.clear()
  }
}