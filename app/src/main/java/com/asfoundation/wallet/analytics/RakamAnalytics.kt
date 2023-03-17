package com.asfoundation.wallet.analytics

import android.app.Application
import android.content.Context
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.core.utils.properties.HostProperties
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.gamification.repository.entity.WalletOrigin
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.identification.IdsRepository
import com.asfoundation.wallet.logging.RakamReceiver
import com.asfoundation.wallet.promo_code.repository.PromoCode
import com.asfoundation.wallet.promo_code.repository.PromoCodeLocalDataSource
import com.asfoundation.wallet.promotions.model.PromotionsModel
import com.appcoins.wallet.core.utils.android_common.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.hilt.android.qualifiers.ApplicationContext
import io.rakam.api.Rakam
import io.rakam.api.RakamClient
import io.rakam.api.TrackingOptions
import io.reactivex.Completable
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import org.json.JSONException
import org.json.JSONObject
import java.net.MalformedURLException
import java.net.URL
import javax.inject.Inject
import javax.inject.Named

@BoundTo(supertype = AnalyticsSetup::class)
@Named("RakamAnalytics")
class RakamAnalytics @Inject constructor(
  @ApplicationContext private val context: Context,
  private val idsRepository: IdsRepository,
  private val promotionsRepository: PromotionsRepository,
  private val logger: com.appcoins.wallet.core.utils.jvm_common.Logger,
  private val promoCodeLocalDataSource: PromoCodeLocalDataSource
) : AnalyticsSetup {

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

  override fun setWalletOrigin(origin: PromotionsModel.WalletOrigin) {
    val superProperties = rakamClient.superProperties ?: JSONObject()
    try {
      superProperties.put("wallet_origin", origin)
    } catch (e: JSONException) {
      e.printStackTrace()
    }

    rakamClient.superProperties = superProperties
  }

  override fun setPromoCode(promoCode: PromoCode) {
    val superProperties = rakamClient.superProperties ?: JSONObject()
    try {
      superProperties.put("promo_code", promoCode)
    } catch (e: JSONException) {
      e.printStackTrace()
    }

    rakamClient.superProperties = superProperties
  }

  fun initialize(): Completable {
    return Single.just(idsRepository.getAndroidId())
      .flatMap { deviceId: String -> startRakam(deviceId) }
      .flatMap { rakamClient: RakamClient ->
        Single.zip(idsRepository.getInstallerPackage(BuildConfig.APPLICATION_ID),
          Single.just(idsRepository.getGamificationLevel()), Single.just(hasGms()),
          Single.just(idsRepository.getActiveWalletAddress()),
          promoCodeLocalDataSource.getSavedPromoCode(),
          { installerPackage: String, level: Int, hasGms: Boolean, walletAddress: String, promoCode: PromoCode ->
            RakamInitializeWrapper(installerPackage, level, hasGms, walletAddress, promoCode)
          })
          .flatMap {
            promotionsRepository.getWalletOrigin(
              it.walletAddress,
              it.promoCode.code
            )
              .doOnSuccess { walletOrigin ->
                setRakamSuperProperties(
                  rakamClient, it.installerPackage,
                  it.level, it.walletAddress, it.hasGms, walletOrigin
                )
                if (!BuildConfig.DEBUG) {
                  logger.addReceiver(RakamReceiver())
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
      instance.initialize(
        context, URL(HostProperties.RAKAM_BASE_HOST),
        BuildConfig.RAKAM_API_KEY
      )
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

  private fun setRakamSuperProperties(
    instance: RakamClient, installerPackage: String,
    userLevel: Int,
    userId: String, hasGms: Boolean, walletOrigin: WalletOrigin
  ) {
    val superProperties = instance.superProperties ?: JSONObject()
    try {
      superProperties.put(
        AnalyticsLabels.APTOIDE_PACKAGE,
        BuildConfig.APPLICATION_ID
      )
      superProperties.put(AnalyticsLabels.VERSION_CODE, BuildConfig.VERSION_CODE)
      superProperties.put(
        AnalyticsLabels.ENTRY_POINT,
        if (installerPackage.isEmpty()) "other" else installerPackage
      )
      superProperties.put(AnalyticsLabels.USER_LEVEL, userLevel)
      superProperties.put(AnalyticsLabels.HAS_GMS, hasGms)
      superProperties.put(AnalyticsLabels.WALLET_ORIGIN, walletOrigin)
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

private data class RakamInitializeWrapper(
  val installerPackage: String, val level: Int,
  val hasGms: Boolean, val walletAddress: String,
  val promoCode: PromoCode
)