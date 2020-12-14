package com.asfoundation.wallet.analytics

import android.app.Application
import android.content.Context
import android.util.Log
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.identification.IdsRepository
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.logging.RakamReceiver
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import io.rakam.api.Rakam
import io.rakam.api.RakamClient
import io.rakam.api.TrackingOptions
import io.reactivex.Completable
import io.reactivex.Single
import org.json.JSONException
import org.json.JSONObject
import java.net.MalformedURLException
import java.net.URL

class RakamAnalytics(private val context: Context, private val idsRepository: IdsRepository,
                     private val logger: Logger) : AnalyticsSetup {

  private val rakamClient = Rakam.getInstance()

  companion object {
    private val TAG = RakamAnalytics::class.java.simpleName
  }

  override fun setUserId(walletAddress: String) {
    rakamClient.setUserId(walletAddress)
  }

  override fun setGamificationLevel(level: Int) {
    val superProperties = rakamClient.superProperties ?: JSONObject()
    try {
      superProperties.put("user_level", level)
    } catch (e: JSONException) {
      e.printStackTrace()
    }

    rakamClient.superProperties = superProperties
  }

  fun initialize(): Completable {
    return Single.just(idsRepository.getAndroidId())
        .flatMap { deviceId: String -> startRakam(deviceId) }
        .flatMap { rakamClient: RakamClient ->
          idsRepository.getInstallerPackage(BuildConfig.APPLICATION_ID)
              .flatMap { installerPackage: String ->
                Single.just(idsRepository.getGamificationLevel())
                    .flatMap { level: Int ->
                      Single.just(hasGms())
                          .flatMap { hasGms: Boolean ->
                            Single.just(idsRepository.getActiveWalletAddress())
                                .doOnSuccess { walletAddress: String ->
                                  setRakamSuperProperties(rakamClient, installerPackage, level,
                                      walletAddress, hasGms)
                                  if (!BuildConfig.DEBUG) {
                                    logger.addReceiver(RakamReceiver())
                                  }
                                }
                          }
                    }
              }
        }
        .ignoreElement()
  }

  private fun startRakam(deviceId: String): Single<RakamClient> {
    val instance = Rakam.getInstance()
    val options = TrackingOptions()
    options.disableAdid()
    try {
      instance.initialize(context, URL(BuildConfig.RAKAM_BASE_HOST),
          BuildConfig.RAKAM_API_KEY)
    } catch (e: MalformedURLException) {
      Log.e(TAG, "error: ", e)
    }
    instance.setTrackingOptions(options)
    instance.deviceId = deviceId
    instance.trackSessionEvents(true)
    instance.setLogLevel(Log.VERBOSE)
    instance.setEventUploadPeriodMillis(1)
    instance.enableLogging(true)
    return Single.just(instance)
  }

  private fun setRakamSuperProperties(instance: RakamClient, installerPackage: String,
                                      userLevel: Int,
                                      userId: String, hasGms: Boolean) {
    val superProperties = instance.superProperties ?: JSONObject()
    try {
      superProperties.put(RakamEventLogger.APTOIDE_PACKAGE,
          BuildConfig.APPLICATION_ID)
      superProperties.put(RakamEventLogger.VERSION_CODE, BuildConfig.VERSION_CODE)
      superProperties.put(RakamEventLogger.ENTRY_POINT,
          if (installerPackage.isEmpty()) "other" else installerPackage)
      superProperties.put(RakamEventLogger.USER_LEVEL, userLevel)
      superProperties.put(RakamEventLogger.HAS_GMS, hasGms)
    } catch (e: JSONException) {
      e.printStackTrace()
    }
    instance.superProperties = superProperties
    if (userId.isNotEmpty()) instance.setUserId(userId)
    instance.enableForegroundTracking(context.applicationContext as Application)
  }

  private fun hasGms(): Boolean {
    return GoogleApiAvailability.getInstance()
        .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
  }
}