package com.asfoundation.wallet.util

import java.util.concurrent.ScheduledThreadPoolExecutor


class SyncExecutor(corePoolSize: Int) : ScheduledThreadPoolExecutor(corePoolSize)