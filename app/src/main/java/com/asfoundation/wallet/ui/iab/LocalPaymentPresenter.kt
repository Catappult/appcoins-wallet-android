package com.asfoundation.wallet.ui.iab

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class LocalPaymentPresenter(private val view: LocalPaymentView,
                            private val amount: String?,
                            private val currency: String?,
                            private val domain: String,
                            private val skuId: String,
                            private val paymentId: String,
                            private val localPaymentInteractor: LocalPaymentInteractor,
                            private val navigator: FragmentNavigator,
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
            paymentId).observeOn(AndroidSchedulers.mainThread()).doOnSuccess {
          //   navigator.navigateToUriForResult(it, "", domain, skuId, null, "")
        }.subscribeOn(Schedulers.io()).subscribe())
  }

}
