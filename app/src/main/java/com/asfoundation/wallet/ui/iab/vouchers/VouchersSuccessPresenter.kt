package com.asfoundation.wallet.ui.iab.vouchers

import io.reactivex.disposables.CompositeDisposable

class VouchersSuccessPresenter(private val view: VouchersSuccessView,
                               private val disposables: CompositeDisposable,
                               private val data: VouchersSuccessData,
                               private val navigator: VouchersSuccessNavigator) {

  fun present() {
    view.setupUi(data.bonus, data.code, data.redeem)
    handleGotItClick()
  }


  private fun handleGotItClick() {
    disposables.add(view.getGotItClick()
        .doOnNext { navigator.finish() }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() = disposables.clear()
}
