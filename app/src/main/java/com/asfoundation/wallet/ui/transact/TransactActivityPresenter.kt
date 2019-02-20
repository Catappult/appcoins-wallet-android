package com.asfoundation.wallet.ui.transact

class TransactActivityPresenter(private val view: TransactActivityView) {
  fun present(isCreating: Boolean) {
    if (isCreating) {
      view.showTransactFragment()
    }
  }

}
