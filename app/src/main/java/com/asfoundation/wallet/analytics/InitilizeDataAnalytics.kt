package com.asfoundation.wallet.analytics

import android.content.Context
import android.content.res.Configuration
import com.appcoins.wallet.core.analytics.analytics.AnalyticsLabels
import com.appcoins.wallet.core.analytics.analytics.IndicativeAnalytics
import com.appcoins.wallet.core.analytics.analytics.partners.PartnerAddressService
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.feature.promocode.data.repository.PromoCode
import com.appcoins.wallet.feature.promocode.data.repository.PromoCodeLocalDataSource
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.identification.DeviceInformation
import com.asfoundation.wallet.identification.IdsRepository
import com.asfoundation.wallet.logging.SentryReceiver
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.indicative.client.android.Indicative
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Completable
import io.reactivex.Single
import io.sentry.Sentry
import io.sentry.SentryLevel
import io.sentry.SentryOptions
import io.sentry.android.core.SentryAndroid
import io.sentry.protocol.User
import kotlin.random.Random
import javax.inject.Inject

/**
 * Stack frame limit for Sentry.
 */
private const val SENTRY_STACK_FRAME_SIZE = 25

class InitilizeDataAnalytics @Inject constructor(
  @ApplicationContext private val context: Context,
  private val idsRepository: IdsRepository,
  private val logger: Logger,
  private val promotionsRepository: PromotionsRepository,
  private val indicativeAnalytics: IndicativeAnalytics,
  private val partnerAddressService: PartnerAddressService,
  private val promoCodeLocalDataSource: PromoCodeLocalDataSource
) {

  fun initializeSentry(): Completable {
    SentryAndroid.init(context) { options ->
      options.dsn = BuildConfig.SENTRY_DSN_KEY
      options.maxBreadcrumbs = 25
      options.tracesSampleRate = 0.25
      options.environment = if (BuildConfig.DEBUG) "DEV" else "PROD"
      options.profilesSampleRate = 1.0
      options.beforeSend = SentryOptions.BeforeSendCallback { event, _ ->
        event.exceptions?.forEach { exception ->
          exception.stacktrace?.frames?.let { frames ->
            if (frames.size > SENTRY_STACK_FRAME_SIZE) {
              frames.subList(0, frames.size - SENTRY_STACK_FRAME_SIZE)
            }
          }
        }
        when (event.level) {
          SentryLevel.DEBUG -> if (BuildConfig.DEBUG) event else null
          SentryLevel.INFO -> if (Random.nextDouble() < 0.25) event else null
          SentryLevel.WARNING -> if (Random.nextDouble() < 0.5) event else null
          else -> event // ERROR and above always sent (1.0)
        }
      }
    }
    val walletAddress = idsRepository.getActiveWalletAddress()

    val user = User().apply {
      id = walletAddress
    }

    Sentry.setUser(user)
    Sentry.setTag(AnalyticsLabels.USER_LEVEL, idsRepository.getGamificationLevel().toString())
    Sentry.setTag(AnalyticsLabels.HAS_GMS, hasGms().toString())

    logger.addReceiver(SentryReceiver())

    return Completable.mergeArray(
      idsRepository.getInstallerPackage(BuildConfig.APPLICATION_ID)
        .doOnSuccess {
          Sentry.setTag(AnalyticsLabels.ENTRY_POINT, it.ifEmpty { "other" })
        }
        .ignoreElement()
        .onErrorComplete(),
      promoCodeLocalDataSource.getSavedPromoCode()
        .flatMap {
          promotionsRepository.getWalletOrigin(walletAddress, it.code)
        }
        .doOnSuccess {
          Sentry.setTag(AnalyticsLabels.WALLET_ORIGIN, it.toString())
        }
        .ignoreElement()
        .onErrorComplete()
    )
  }

  fun initializeIndicative(): Completable {
    Indicative.launch(context, BuildConfig.INDICATIVE_API_KEY)
    return Single.just(idsRepository.getAndroidId())
      .flatMap { _: String ->
        Single.zip(
          idsRepository.getInstallerPackage(BuildConfig.APPLICATION_ID),
          Single.just(idsRepository.getGamificationLevel()),
          Single.just(hasGms()),
          Single.just(idsRepository.getActiveWalletAddress()),
          promoCodeLocalDataSource.getSavedPromoCode(),
          Single.just(idsRepository.getDeviceInfo()),
          partnerAddressService.getOrSetOemIDFromGamesHub(),
          Single.just(getTheme())
        )
        { installerPackage: String, level: Int, hasGms: Boolean, walletAddress: String, promoCode: PromoCode, deviceInfo: DeviceInformation, ghOemid: String, theme: String ->
          IndicativeInitializeWrapper(
            installerPackage = installerPackage,
            level = level,
            hasGms = hasGms,
            walletAddress = walletAddress,
            promoCode = promoCode,
            deviceInfo = deviceInfo,
            ghOemId = ghOemid,
            theme = theme
          )
        }
          .flatMap {
            promotionsRepository.getWalletOrigin(
              it.walletAddress,
              it.promoCode.code
            )
              .doOnSuccess { walletOrigin ->
                indicativeAnalytics.setIndicativeSuperProperties(
                  installerPackage = it.installerPackage,
                  userLevel = it.level,
                  userId = it.walletAddress,
                  hasGms = it.hasGms,
                  walletOrigin = walletOrigin.name,
                  osVersion = it.deviceInfo.osVersion,
                  brand = it.deviceInfo.brand,
                  model = it.deviceInfo.model,
                  language = it.deviceInfo.language,
                  isEmulator = it.deviceInfo.isProbablyEmulator,
                  ghOemId = it.ghOemId,
                  promoCode = it.promoCode.code ?: "",
                  flavor = mapFlavor(),
                  theme = it.theme
                )
              }
          }
      }
      .ignoreElement()
  }

  private fun mapFlavor(flavor: String = BuildConfig.FLAVOR): String {
    return when (flavor) {
      "aptoide" -> "aptoide"
      "gp" -> "google"
      else -> flavor
    }
  }

  fun getTheme(): String {
    val currentNightMode =
      context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return mapTheme(currentNightMode == Configuration.UI_MODE_NIGHT_YES)
  }

  private fun mapTheme(isDarkModeOn: Boolean): String {
    return if (isDarkModeOn) "dark" else "light"
  }

  private fun hasGms(): Boolean {
    return GoogleApiAvailability.getInstance()
      .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
  }
}
