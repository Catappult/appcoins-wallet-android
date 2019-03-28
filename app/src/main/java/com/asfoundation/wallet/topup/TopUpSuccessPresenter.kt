package com.asfoundation.wallet.topup

import io.reactivex.disposables.CompositeDisposable

class TopUpSuccessPresenter(private val view: TopUpSuccessFragmentView) {
  private val disposables: CompositeDisposable = CompositeDisposable()

  fun present() {
    view.show()

    handleOKClick()
  }

  fun stop() {
    disposables.clear()
    view.clean()
  }

  private fun handleOKClick() {
    disposables.add(
        view.getOKClicks().doOnNext {
          view.close()
        }.subscribe())
  }

}
