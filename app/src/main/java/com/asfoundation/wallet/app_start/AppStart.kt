package com.asfoundation.wallet.app_start

import com.asfoundation.wallet.di.IoDispatcher
import dagger.Reusable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.net.URLDecoder
import javax.inject.Inject

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

@Reusable
class AppStartUseCase @Inject constructor(
  private val repository: AppStartRepository,
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
      val referrer = repository.getReferrerUrl()?.splitQuery()
      if (referrer.isCorrectUtm()) {
        StartMode.FirstUtm(
          sku = referrer?.get(UTM_CONTENT)?.get(0) ?: "",
          source = referrer?.get(UTM_SOURCE)?.get(0) ?: "",
          packageName = referrer?.get(UTM_MEDIUM)?.get(0) ?: "",
          integrationFlow = referrer?.get(UTM_TERM)?.get(0) ?: "",
        )
      } else {
        StartMode.First
      }
    } else {
      StartMode.Subsequent
    }

    _startModes.emit(mode)
  }

  val startModes: Flow<StartMode> get() = _startModes

  private fun Map<String, List<String>>?.isCorrectUtm(): Boolean =
    this?.get(UTM_TERM)?.get(0) in ALLOWED_TERMS
        && this?.get(UTM_MEDIUM)?.get(0)?.isEmpty() == false

  private fun String.splitQuery(): Map<String, List<String>> =
    URLDecoder.decode(this, "UTF-8").split("&")
      .map { it.split("=") }
      .mapNotNull {
        if (it.size < 2) null else {
          it[0] to it[1]
        }
      }
      .groupBy { it.first }
      .mapValues { entry -> entry.value.map { it.second } }

  companion object {
    val ALLOWED_TERMS = listOf("sdk", "osp")

    const val UTM_SOURCE = "utm_source"
    const val UTM_MEDIUM = "utm_medium"
    const val UTM_TERM = "utm_term"
    const val UTM_CONTENT = "utm_content"
  }
}

interface AppStartRepository {
  suspend fun getRunCount(): Int
  suspend fun saveRunCount(count: Int)
  suspend fun getFirstInstallTime(): Long
  suspend fun getLastUpdateTime(): Long
  suspend fun getReferrerUrl(): String?

  companion object {
    internal const val RUNS_COUNT = "AppStartRepository.RunsCount"
  }
}
