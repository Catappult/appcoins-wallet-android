package com.asfoundation.wallet.app_start

import android.content.Context
import android.content.pm.PackageManager
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.appcoins.wallet.commons.Logger
import com.asf.wallet.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import it.czerwinski.android.hilt.annotations.BoundTo
import kotlinx.coroutines.suspendCancellableCoroutine
import repository.AppStartSharedPreferences
import java.util.concurrent.TimeoutException
import javax.inject.Inject

@BoundTo(supertype = AppStartRepository::class)
class AppStartRepositoryImpl @Inject constructor(
  private val packageManager: PackageManager,
  private val appStartSharedPreferences: AppStartSharedPreferences
) : AppStartRepository {

  override suspend fun getRunCount(): Int = appStartSharedPreferences.getRunCount()

  override suspend fun saveRunCount(count: Int) = appStartSharedPreferences.saveRunCount(count)

  override suspend fun getFirstInstallTime() =
    packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0).firstInstallTime

  override suspend fun getLastUpdateTime() =
    packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0).lastUpdateTime
}

@BoundTo(supertype = GooglePlayInstallRepository::class)
class GooglePlayInstallRepositoryImpl @Inject constructor(
  @ApplicationContext private val context: Context,
  private val logger: Logger
) : GooglePlayInstallRepository {

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
    private const val TAG = "GooglePlayInstallRepository"
  }
}