package com.asfoundation.wallet.ui.balance

import com.appcoins.wallet.feature.walletInfo.data.FindDefaultWalletInteract
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class QrCodePresenter(
    val view: QrCodeView,
    val findDefaultWalletInteract: com.appcoins.wallet.feature.walletInfo.data.FindDefaultWalletInteract,
    val disposable: CompositeDisposable,
    val viewScheduler: Scheduler) {

  fun present() {
    requestActiveWalletAddress()
  }

  private fun requestActiveWalletAddress() {
    disposable.add(
        findDefaultWalletInteract.find()
            .observeOn(viewScheduler)
            .doOnSuccess {
              view.createQrCode(it.address)
            }
            .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() {
    disposable.clear()
  }
}