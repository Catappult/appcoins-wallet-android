package com.asfoundation.wallet.app_start

import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

interface FirstTopAppUseCase {
  suspend operator fun invoke(): StartMode.FirstTopApp?
}

@BoundTo(supertype = FirstTopAppUseCase::class)
class FirstTopAppUseCaseImpl @Inject constructor(
  private val repository: FirstTopAppRepository
) : FirstTopAppUseCase {

  override suspend operator fun invoke(): StartMode.FirstTopApp? =
    repository.getInstalledPackages()
      .toSet()
      .run { TOP_APPS_PACKAGES.filter { contains(it) } }
      .takeIf { it.size == 1 }
      ?.first()
      ?.let { StartMode.FirstTopApp(it) }

  companion object {
    val TOP_APPS_PACKAGES = listOf(
      "com.igg.android.lordsmobile",
      "com.hcg.cok.gp",
      "com.mobile.legends",
      "com.kingsgroup.ww2",
      "com.san.dias.xhbz.catap"
    )
  }
}

interface FirstTopAppRepository {
  suspend fun getInstalledPackages(): List<String>
}
