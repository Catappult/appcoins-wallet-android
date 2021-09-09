package com.asfoundation.wallet.analytics

import android.annotation.SuppressLint
import android.content.Context
import com.amplitude.api.Amplitude
import com.amplitude.api.Identify
import com.amplitude.api.TrackingOptions
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.identification.IdsRepository
import com.asfoundation.wallet.promotions.model.PromotionsModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import io.reactivex.schedulers.Schedulers

class AmplitudeAnalytics(private val context: Context, private val idsRepository: IdsRepository) :
    AnalyticsSetup {

  private val amplitudeClient = Amplitude.getInstance()
  private lateinit var entryPoint: String

  override fun setUserId(walletAddress: String) {
    amplitudeClient.userId = walletAddress
  }

  override fun setGamificationLevel(level: Int) {
    val identify = Identify().append(AmplitudeEventLogger.USER_LEVEL, level)
        .append(AmplitudeEventLogger.APTOIDE_PACKAGE, BuildConfig.APPLICATION_ID)
        .append(AmplitudeEventLogger.VERSION_CODE, BuildConfig.VERSION_CODE)
        .append(AmplitudeEventLogger.ENTRY_POINT,
            if (entryPoint.isEmpty()) "other" else entryPoint)
        .append(AmplitudeEventLogger.HAS_GMS, hasGms())

    amplitudeClient.identify(identify)
  }

  override fun setWalletOrigin(origin: PromotionsModel.WalletOrigin) {
    // Not used
  }

  @SuppressLint("CheckResult")
  fun start() {
    idsRepository.getInstallerPackage(BuildConfig.APPLICATION_ID)
        .doOnSuccess { installerPackage ->
          val userId = idsRepository.getActiveWalletAddress()
          val userLevel = idsRepository.getGamificationLevel()
          amplitudeClient.initialize(context, BuildConfig.AMPLITUDE_API_KEY, userId)
              .setTrackingOptions(TrackingOptions().disableAdid())
          setAmplitudeSuperProperties(installerPackage, userLevel)
        }
        .subscribeOn(Schedulers.io())
        .subscribe({}, { it.printStackTrace() })
  }

  private fun setAmplitudeSuperProperties(installerPackage: String,
                                          userLevel: Int) {
    entryPoint = if (installerPackage.isEmpty()) "other" else installerPackage
    val identify = Identify().append(AmplitudeEventLogger.USER_LEVEL, userLevel)
        .append(AmplitudeEventLogger.APTOIDE_PACKAGE, BuildConfig.APPLICATION_ID)
        .append(AmplitudeEventLogger.VERSION_CODE, BuildConfig.VERSION_CODE)
        .append(AmplitudeEventLogger.ENTRY_POINT, entryPoint)
        .append(AmplitudeEventLogger.HAS_GMS, hasGms())

    amplitudeClient.identify(identify)
  }

  private fun hasGms(): Boolean {
    return GoogleApiAvailability.getInstance()
        .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
  }
}