package com.asfoundation.wallet.app_start

import it.czerwinski.android.hilt.annotations.BoundTo
import kotlinx.coroutines.withTimeoutOrNull
import java.net.URLDecoder
import javax.inject.Inject

interface GPInstallUseCase {
  suspend operator fun invoke(): StartMode.GPInstall?
}

@BoundTo(supertype = GPInstallUseCase::class)
class GPInstallUseCaseImpl @Inject constructor(
  private val repository: GooglePlayInstallRepository
) : GPInstallUseCase {
  override suspend operator fun invoke(): StartMode.GPInstall? {
    val referrer = withTimeoutOrNull(5000) { repository.getReferrerUrl() }
      ?.splitQuery()
    return if (referrer.isCorrectUtm()) {
      StartMode.GPInstall(
        sku = referrer?.get(UTM_CONTENT)?.get(0) ?: "",
        source = referrer?.get(UTM_SOURCE)?.get(0) ?: "",
        packageName = referrer?.get(UTM_MEDIUM)?.get(0) ?: "",
        integrationFlow = referrer?.get(UTM_TERM)?.get(0) ?: "",
      )
    } else {
      null
    }
  }

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
    private val ALLOWED_TERMS = listOf("sdk", "osp")

    const val UTM_SOURCE = "utm_source"
    const val UTM_MEDIUM = "utm_medium"
    const val UTM_TERM = "utm_term"
    const val UTM_CONTENT = "utm_content"
  }
}

interface GooglePlayInstallRepository {
  suspend fun getReferrerUrl(): String?
}
