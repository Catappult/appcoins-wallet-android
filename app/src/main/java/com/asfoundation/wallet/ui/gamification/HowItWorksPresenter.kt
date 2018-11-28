package com.asfoundation.wallet.ui.gamification

import android.os.Bundle
import io.reactivex.disposables.CompositeDisposable

class HowItWorksPresenter(private val view: HowItWorksView) {
  val disposables = CompositeDisposable()
  fun present(savedInstanceState: Bundle?) {
    handleOkClick()
  }

  private fun handleOkClick() {
    disposables.add(view.getOkClick().doOnNext { view.close() }.subscribe())
  }

  fun stop() {
//    TODO(
//        "not implemented") //To change body of created functions use File | Settings | File Templates.
  }

}
