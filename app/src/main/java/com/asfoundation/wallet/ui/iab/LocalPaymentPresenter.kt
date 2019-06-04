package com.asfoundation.wallet.ui.iab

import io.reactivex.disposables.CompositeDisposable

class LocalPaymentPresenter(private val view: LocalPaymentView,
                            private val amount: String,
                            private val currency: String?,
                            private val domain: String,
                            private val skuId: String,
                            private val uri: String?,
                            private val isBds: Boolean,
                            private val paymentId: String,
                            private val localPaymentInteractor: LocalPaymentInteractor,
                            private val disposables: CompositeDisposable) {
  fun present() {
    onViewCreatedRequestLink()
  }

  private fun handleStop() {
    disposables.clear()
  }

  private fun onViewCreatedRequestLink() {
    disposables.add(
        localPaymentInteractor.getPaymentLink(domain, skuId, amount, currency,
            paymentId).doOnSuccess { view.showLink(it) }.subscribe())
  }

}
