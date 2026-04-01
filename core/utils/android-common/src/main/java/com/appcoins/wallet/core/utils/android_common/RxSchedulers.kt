package com.appcoins.wallet.core.utils.android_common

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

interface RxSchedulers {
  val main: Scheduler
  val io: Scheduler
  val computation: Scheduler
}

class RxSchedulersImpl @Inject constructor() : RxSchedulers {
  override val main: Scheduler = AndroidSchedulers.mainThread()
  override val io: Scheduler = Schedulers.io()
  override val computation: Scheduler = Schedulers.computation()
}