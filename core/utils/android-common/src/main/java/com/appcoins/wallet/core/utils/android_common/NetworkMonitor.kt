package com.appcoins.wallet.core.utils.android_common

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
  val isConnected: Flow<Boolean>
}