package com.asfoundation.wallet.ui.transact

class TransferActivityPresenter(private val view: TransferActivityView) {
  fun present(isCreating: Boolean) {
    if (isCreating) {
      view.showTransactFragment()
    }
  }

}
