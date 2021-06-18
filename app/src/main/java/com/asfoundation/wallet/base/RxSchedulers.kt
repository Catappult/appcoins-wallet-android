package com.asfoundation.wallet.base

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

interface RxSchedulers {
  val main: Scheduler
  val io: Scheduler
  val computation: Scheduler
}

class RxSchedulersImpl : RxSchedulers {
  override val main: Scheduler = AndroidSchedulers.mainThread()
  override val io: Scheduler = Schedulers.io()
  override val computation: Scheduler = Schedulers.computation()
}