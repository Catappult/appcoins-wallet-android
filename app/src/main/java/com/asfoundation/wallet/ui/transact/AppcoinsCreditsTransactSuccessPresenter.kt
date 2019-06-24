package com.asfoundation.wallet.ui.transact

import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal

class AppcoinsCreditsTransactSuccessPresenter(private val view: AppcoinsCreditsTransactSuccessView,
                                              private val amount: BigDecimal,
                                              private val currency: String,
                                              private val toAddress: String,
                                              private val disposables: CompositeDisposable) {
  fun present() {
    view.setup(amount, currency, toAddress)
    handleOkButtonClick()
  }

  private fun handleOkButtonClick() {
    disposables.add(view.getOkClick().doOnNext { view.close() }.subscribe())
  }

  fun stop() {
    disposables.clear()
  }

}
