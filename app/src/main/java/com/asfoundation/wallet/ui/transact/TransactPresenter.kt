package com.asfoundation.wallet.ui.transact

import io.reactivex.disposables.CompositeDisposable

class TransactPresenter(private val view: TransactFragmentView,
                        private val disposables: CompositeDisposable) {
  fun present() {
    handleButtonClick()
  }

  private fun handleButtonClick() {
    disposables.add(view.getSendClick().subscribe())
  }
}
