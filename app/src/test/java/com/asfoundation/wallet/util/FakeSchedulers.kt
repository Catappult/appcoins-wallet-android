package com.asfoundation.wallet.util

import com.asfoundation.wallet.base.RxSchedulers
import io.reactivex.Scheduler
import io.reactivex.schedulers.TestScheduler

class FakeSchedulers : RxSchedulers {
  var testScheduler: Scheduler = TestScheduler()

  override val main: Scheduler
    get() = testScheduler
  override val io: Scheduler
    get() = testScheduler
  override val computation: Scheduler
    get() = testScheduler
}