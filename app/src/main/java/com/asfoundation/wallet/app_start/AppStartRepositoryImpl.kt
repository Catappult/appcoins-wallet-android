package com.asfoundation.wallet.app_start

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.app_start.AppStartRepository.Companion.RUNS_COUNT
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

class AppStartRepositoryImpl @Inject constructor(
  @ApplicationContext val context: Context,
  private val packageManager: PackageManager,
  private val pref: SharedPreferences
) : AppStartRepository {

  private val referrerClient: InstallReferrerClient by lazy {
    InstallReferrerClient.newBuilder(
      context
    ).build()
  }

  override suspend fun getRunCount(): Int = pref.getInt(RUNS_COUNT, 0)

  override suspend fun saveRunCount(count: Int) = pref.edit()
    .putInt(RUNS_COUNT, count)
    .apply()

  override suspend fun getFirstInstallTime() =
    packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0).firstInstallTime

  override suspend fun getLastUpdateTime() =
    packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0).lastUpdateTime

  override suspend fun getReferrerUrl(): String? = suspendCoroutine { cont ->
    referrerClient.startConnection(object : InstallReferrerStateListener {
      override fun onInstallReferrerSetupFinished(responseCode: Int) {
        val referrerUrl = if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
          referrerClient.installReferrer.installReferrer
        } else {
          null
        }
        cont.resumeWith(Result.success(referrerUrl))
        referrerClient.endConnection()
      }

      override fun onInstallReferrerServiceDisconnected() {}
    })
  }
}
