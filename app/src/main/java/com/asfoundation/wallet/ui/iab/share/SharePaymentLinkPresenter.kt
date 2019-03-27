package com.asfoundation.wallet.ui.iab.share

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class SharePaymentLinkPresenter(private val view: SharePaymentLinkFragmentView,
                                private val interactor: ShareLinkInteractor,
                                private val viewScheduler: Scheduler,
                                private val networkScheduler: Scheduler,
                                private val disposables: CompositeDisposable) {


  fun present() {
    handleStop()
    handleShare()
  }

  private fun handleShare() {
    disposables.add(view.getShareButtonClick()
        .doOnNext { view.showFetchingLinkInfo() }
        .flatMapSingle {
          interactor.getLinkToShare(it.domain, it.skuId, it.message).subscribeOn(
              networkScheduler)
        }.observeOn(viewScheduler).doOnNext {
          view.shareLink(it)
        }.subscribe({}, { view.showErrorInfo() }))
  }

  private fun handleStop() {
    disposables.add(view.getCancelButtonClick().map {
      view.close()
    }.subscribe())
  }

  fun stop() {
    disposables.clear()
  }

}
