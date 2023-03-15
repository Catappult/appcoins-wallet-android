package com.appcoins.wallet.core.utils.common

import java.util.concurrent.ScheduledThreadPoolExecutor


class SyncExecutor(corePoolSize: Int) : ScheduledThreadPoolExecutor(corePoolSize)