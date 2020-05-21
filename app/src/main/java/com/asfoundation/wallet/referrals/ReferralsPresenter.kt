package com.asfoundation.wallet.referrals

import io.reactivex.disposables.CompositeDisposable

class ReferralsPresenter(private val view: ReferralsView,
                         private val disposables: CompositeDisposable) {

  fun present() {
    view.setupLayout()
    handleBottomSheetHeaderClick()
  }

  private fun handleBottomSheetHeaderClick() {
    disposables.add(view.bottomSheetHeaderClick()
        .doOnNext { view.changeBottomSheetState() }
        .subscribe({}, { it.printStackTrace() }))
  }

}
