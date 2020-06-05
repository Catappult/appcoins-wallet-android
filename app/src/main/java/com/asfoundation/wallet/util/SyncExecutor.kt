package com.asfoundation.wallet.util

import android.util.Log
import java.util.concurrent.ScheduledThreadPoolExecutor


class SyncExecutor : ScheduledThreadPoolExecutor {
  constructor(corePoolSize: Int) : super(corePoolSize) {
    this.maximumPoolSize = corePoolSize
  }

  override fun beforeExecute(t: Thread?, r: Runnable?) {
    Log.e("TEST", "*** Before execute [thread: ${t?.id}], maximum pool size $poolSize and $maximumPoolSize")
    super.beforeExecute(t, r)
  }

  override fun afterExecute(r: Runnable?, t: Throwable?) {
    Log.e("TEST", "*** After execute")
    super.afterExecute(r, t)
  }
}