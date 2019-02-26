package com.asfoundation.wallet.ui.transact

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

class TransactPresenter(private val view: TransactFragmentView,
                        private val disposables: CompositeDisposable,
                        private val interactor: TransferInteractor,
                        private val ioScheduler: Scheduler,
                        private val viewScheduler: Scheduler,
                        private val packageName: String) {
  companion object {
    private val TAG = TransactPresenter::class.java.simpleName
  }

  fun present() {
    handleButtonClick()
  }

  private fun handleButtonClick() {
    disposables.add(view.getSendClick()
        .subscribeOn(viewScheduler)
        .observeOn(ioScheduler)
        .flatMapSingle {
          return@flatMapSingle when (it.currency) {
            TransactFragmentView.Currency.APPC_C -> interactor.transferCredits(it.walletAddress,
                it.amount, packageName)
            else -> Single.error { UnsupportedOperationException("${it.currency} not supported") }
          }

        }.doOnError { error -> error.printStackTrace() }
        .retry()
        .subscribe { })
  }

  fun clear() {
    disposables.clear()
  }
}