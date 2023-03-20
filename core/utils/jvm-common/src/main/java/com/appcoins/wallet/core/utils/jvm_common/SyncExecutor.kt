package com.appcoins.wallet.core.utils.jvm_common

import java.util.concurrent.ScheduledThreadPoolExecutor


class SyncExecutor(corePoolSize: Int) : ScheduledThreadPoolExecutor(corePoolSize)