package com.asfoundation.wallet.util

import com.appcoins.wallet.ui.arch.RxSchedulers
import io.reactivex.schedulers.TestScheduler

class FakeSchedulers : com.appcoins.wallet.ui.arch.RxSchedulers {
  var testScheduler = TestScheduler()

  override val main
    get() = testScheduler
  override val io
    get() = testScheduler
  override val computation
    get() = testScheduler
}