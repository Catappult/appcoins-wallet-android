package com.asfoundation.wallet.app_start

import com.asfoundation.wallet.di.IoDispatcher
import com.asfoundation.wallet.onboarding.use_cases.PendingPurchaseFlowUseCase
import dagger.Reusable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class StartMode {
  object Regular : StartMode()
  data class GPInstall(
    val sku: String,
    val source: String,
    val packageName: String,
    val integrationFlow: String,
  ) : StartMode()

  data class PendingPurchaseFlow(
    val integrationFlow: String,
    val sku: String?,
    val packageName: String,
    val callbackUrl: String?,
    val currency: String?,
    val orderReference: String?,
    val value: Double?,
    val signature: String?
  ) : StartMode()

  object Subsequent : StartMode()
}

@Reusable
class AppStartUseCase @Inject constructor(
  private val repository: AppStartRepository,
  private val gpInstallUseCase: GPInstallUseCase,
  private val pendingPurchaseFlowUseCase: PendingPurchaseFlowUseCase,
  @IoDispatcher ioDispatcher: CoroutineDispatcher
) {
  private val scope = CoroutineScope(ioDispatcher)

  private val _startModes =
    MutableSharedFlow<StartMode>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  fun registerAppStart() = scope.launch {
    val firstInstallTime = repository.getFirstInstallTime()
    val lastUpdateTime = repository.getLastUpdateTime()
    val runs = repository.getRunCount() + 1
    repository.saveRunCount(runs)

    val mode = if (firstInstallTime == lastUpdateTime && runs == 1) {
      pendingPurchaseFlowUseCase()
        ?: gpInstallUseCase()
        ?: StartMode.Regular
    } else {
      StartMode.Subsequent
    }

    _startModes.emit(mode)
  }

  val startModes: Flow<StartMode> get() = _startModes
}

interface AppStartRepository {
  suspend fun getRunCount(): Int
  suspend fun saveRunCount(count: Int)
  suspend fun getFirstInstallTime(): Long
  suspend fun getLastUpdateTime(): Long

  companion object {
    internal const val RUNS_COUNT = "AppStartRepository.RunsCount"
  }
}
