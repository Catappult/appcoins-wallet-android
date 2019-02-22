package com.asfoundation.wallet.ui.transact

import android.util.Log
import io.reactivex.disposables.CompositeDisposable

class TransactPresenter(private val view: TransactFragmentView,
                        private val disposables: CompositeDisposable,
                        private val interactor: TransferInteractor) {
  companion object {
    private val TAG = TransactPresenter::class.java.simpleName
  }

  fun present() {
    handleButtonClick()
  }

  private fun handleButtonClick() {
    disposables.add(view.getSendClick().subscribe { Log.d(TAG, "handleButtonClick: $it") })
  }
}