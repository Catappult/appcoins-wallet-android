package com.asfoundation.wallet.transfers

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class EtherTransactionBottomSheetPresenter(private val view: EtherTransactionBottomSheetView,
                                           private val navigator: EtherTransactionBottomSheetNavigator,
                                           private val disposable: CompositeDisposable,
                                           private val viewScheduler: Scheduler,
                                           private val data: EtherTransactionBottomSheetData) {

  fun present() {
    view.setTransactionHash(data.transactionHash)
    handleEtherScanClick()
    handleClipboardClick()
    handleOkClick()
  }

  private fun handleEtherScanClick() {
    disposable.add(view.getEtherScanClick()
        .observeOn(viewScheduler)
        .doOnNext { navigator.navigateToEtherScanTransaction(data.transactionHash) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleClipboardClick() {
    disposable.add(view.getClipboardClick()
        .observeOn(viewScheduler)
        .doOnNext { view.copyToClipboard(data.transactionHash) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleOkClick() {
    disposable.add(view.getOkClick()
        .observeOn(viewScheduler)
        .doOnNext { navigator.goBackToTransactions() }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() = disposable.clear()
}