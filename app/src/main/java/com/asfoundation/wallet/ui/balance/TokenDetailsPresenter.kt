package com.asfoundation.wallet.ui.balance

import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class TokenDetailsPresenter(private val view: TokenDetailsView,
                            private val disposables: CompositeDisposable) {
  fun present() {
    view.setupUi()
    handleOkClick()
    handleTopUpClick()
  }

  fun stop() {
    disposables.dispose()
  }

  private fun handleOkClick() {
    disposables.add(
        view.getOkClick()
            .doOnNext { view.close() }
            .subscribe()
    )
  }

  private fun handleTopUpClick() {
    disposables.add(
        view.getTopUpClick()
            .throttleFirst(1, TimeUnit.SECONDS)
            .doOnNext { view.showTopUp() }
            .subscribe()
    )
  }
}
