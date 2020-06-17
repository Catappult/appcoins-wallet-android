package com.asfoundation.wallet.util

import java.util.concurrent.ScheduledThreadPoolExecutor


class SyncExecutor : ScheduledThreadPoolExecutor {
  constructor(corePoolSize: Int) : super(corePoolSize) {
    this.maximumPoolSize = corePoolSize
  }
}