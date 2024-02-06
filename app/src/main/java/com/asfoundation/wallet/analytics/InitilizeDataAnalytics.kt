package com.asfoundation.wallet.analytics

import android.app.Application
import android.content.Context
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
import io.sentry.android.AndroidSentryClientFactory
import io.sentry.event.User
import javax.inject.Inject

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
        Sentry.init(
            BuildConfig.SENTRY_DSN_KEY,
            AndroidSentryClientFactory(context.applicationContext as Application)
        )
        val walletAddress = idsRepository.getActiveWalletAddress()
        Sentry.getContext().apply {
            user = User(walletAddress, null, null, null)
            addExtra(AnalyticsLabels.USER_LEVEL, idsRepository.getGamificationLevel())
            addExtra(AnalyticsLabels.HAS_GMS, hasGms())
        }
        logger.addReceiver(SentryReceiver())
        return Completable.mergeArray(
            idsRepository.getInstallerPackage(BuildConfig.APPLICATION_ID)
                .doOnSuccess {
                    Sentry.getContext()
                        .addExtra(AnalyticsLabels.ENTRY_POINT, it.ifEmpty { "other" })
                }
                .ignoreElement()
                .onErrorComplete(),
            promoCodeLocalDataSource.getSavedPromoCode()
                .flatMap {
                    promotionsRepository.getWalletOrigin(walletAddress, it.code)
                }
                .doOnSuccess { Sentry.getContext().addExtra(AnalyticsLabels.WALLET_ORIGIN, it) }
                .ignoreElement()
                .onErrorComplete()
        )
    }

    fun initializeIndicative(): Completable {
        Indicative.launch(context, BuildConfig.INDICATIVE_API_KEY);
        return Single.just(idsRepository.getAndroidId())
            .flatMap { deviceId: String ->
                Single.zip(
                    idsRepository.getInstallerPackage(BuildConfig.APPLICATION_ID),
                    Single.just(idsRepository.getGamificationLevel()),
                    Single.just(hasGms()),
                    Single.just(idsRepository.getActiveWalletAddress()),
                    promoCodeLocalDataSource.getSavedPromoCode(),
                    Single.just(idsRepository.getDeviceInfo()),
                    partnerAddressService.getOrSetOemIDFromGamesHub()
                )
                { installerPackage: String, level: Int, hasGms: Boolean, walletAddress: String, promoCode: PromoCode, deviceInfo: DeviceInformation, ghOemid: String ->
                    IndicativeInitializeWrapper(
                        installerPackage = installerPackage,
                        level = level,
                        hasGms = hasGms,
                        walletAddress = walletAddress,
                        promoCode = promoCode,
                        deviceInfo = deviceInfo,
                        ghOemId = ghOemid,
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
                                        ghOemId = it.ghOemId
                                        )
                            }
                    }
            }
            .ignoreElement()
    }

    private fun hasGms(): Boolean {
        return GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    }
}
