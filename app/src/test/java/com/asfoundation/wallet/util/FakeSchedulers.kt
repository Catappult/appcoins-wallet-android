package com.asfoundation.wallet.util

import com.asfoundation.wallet.base.RxSchedulers
import io.reactivex.schedulers.TestScheduler

class FakeSchedulers : RxSchedulers {
  var testScheduler = TestScheduler()

  override val main
    get() = testScheduler
  override val io
    get() = testScheduler
  override val computation
    get() = testScheduler
}