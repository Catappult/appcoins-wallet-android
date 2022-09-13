package com.asfoundation.wallet.app_start

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.appcoins.wallet.commons.Logger
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.app_start.AppStartRepository.Companion.RUNS_COUNT
import dagger.hilt.android.qualifiers.ApplicationContext
import it.czerwinski.android.hilt.annotations.BoundTo
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeoutException
import javax.inject.Inject

@BoundTo(supertype = AppStartRepository::class)
class AppStartRepositoryImpl @Inject constructor(
  @ApplicationContext private val context: Context,
  private val packageManager: PackageManager,
  private val pref: SharedPreferences
) : AppStartRepository {

  override suspend fun getRunCount(): Int = pref.getInt(RUNS_COUNT, 0)

  override suspend fun saveRunCount(count: Int) = pref.edit()
    .putInt(RUNS_COUNT, count)
    .apply()

  override suspend fun getFirstInstallTime() =
    packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0).firstInstallTime

  override suspend fun getLastUpdateTime() =
    packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0).lastUpdateTime
}

@BoundTo(supertype = FirstUtmRepository::class)
class FirstUtmRepositoryImpl @Inject constructor(
  @ApplicationContext private val context: Context,
  private val logger: Logger
) : FirstUtmRepository {

  private val referrerClient: InstallReferrerClient by lazy {
    InstallReferrerClient.newBuilder(
      context
    ).build()
  }

  override suspend fun getReferrerUrl(): String? = suspendCancellableCoroutine { cont ->
    referrerClient.startConnection(object : InstallReferrerStateListener {
      override fun onInstallReferrerSetupFinished(responseCode: Int) {
        val referrerUrl = if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
          referrerClient.installReferrer.installReferrer
        } else {
          null
        }
        if (cont.isActive) {
          cont.resumeWith(Result.success(referrerUrl))
        } else {
          logger.log(TAG, TimeoutException("Install referrer cancelled on timeout"))
        }
        referrerClient.endConnection()
      }

      override fun onInstallReferrerServiceDisconnected() {}
    })
  }

  companion object {
    private const val TAG = "FirstUtmRepository"
  }
}

@BoundTo(supertype = FirstTopAppRepository::class)
class FirstTopAppRepositoryImpl @Inject constructor(
  @ApplicationContext val context: Context,
) : FirstTopAppRepository {

  override suspend fun getInstalledPackages() =
    context.packageManager.getInstalledPackages(0).map { it.packageName }
}
