package com.asfoundation.wallet.ui.balance

import io.reactivex.disposables.CompositeDisposable

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
    disposables.add(view.getOkClick().doOnNext {
      view.close()
    }.subscribe())
  }

  private fun handleTopUpClick() {
    disposables.add(view.getTopUpClick().doOnNext {
      view.showTopUp()
    }.subscribe())
  }


}
