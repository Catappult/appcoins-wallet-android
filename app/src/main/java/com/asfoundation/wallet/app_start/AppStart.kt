package com.asfoundation.wallet.app_start

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

sealed class StartMode {
  object First : StartMode()
  data class FirstUtm(
    val sku: String,
    val source: String,
    val packageName: String,
    val integrationFlow: String,
  ) : StartMode()

  object Subsequent : StartMode()
}

class AppStartUseCase(
  private val repository: AppStartRepository,
  ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
  private val scope = CoroutineScope(ioDispatcher)

  private val _startModes =
    MutableSharedFlow<StartMode>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  fun registerAppStart() = scope.launch {
    repository.saveRunCount(2)
    _startModes.emit(StartMode.Subsequent)
  }

  val startModes: Flow<StartMode> get() = _startModes
}

interface AppStartRepository {
  suspend fun getRunCount(): Int
  suspend fun saveRunCount(count: Int)
  suspend fun getFirstInstallTime(): Long
  suspend fun getLastUpdateTime(): Long
  suspend fun getReferrerUrl(): String?
}
