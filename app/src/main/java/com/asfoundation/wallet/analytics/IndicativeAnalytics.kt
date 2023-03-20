package com.asfoundation.wallet.analytics

import android.content.Context
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.gamification.repository.entity.WalletOrigin
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.identification.DeviceInformation
import com.asfoundation.wallet.identification.IdsRepository
import com.asfoundation.wallet.promo_code.repository.PromoCode
import com.asfoundation.wallet.promo_code.repository.PromoCodeLocalDataSource
import com.asfoundation.wallet.promotions.model.PromotionsModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.indicative.client.android.Indicative
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Completable
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject
import javax.inject.Singleton

@BoundTo(supertype = AnalyticsSetup::class)
@Singleton
class IndicativeAnalytics @Inject constructor(
  @ApplicationContext private val context: Context,
  private val idsRepository: IdsRepository,
  private val promotionsRepository: PromotionsRepository,
  private val logger: Logger,
  private val promoCodeLocalDataSource: PromoCodeLocalDataSource
) : AnalyticsSetup {

  var usrId: String = ""  // wallet address
  var superProperties: MutableMap<String, Any> = HashMap()

  companion object {
    private val TAG = IndicativeAnalytics::class.java.simpleName
  }

  override fun setUserId(walletAddress: String) {
    usrId = walletAddress
  }

  override fun setGamificationLevel(level: Int) {
    superProperties.put(AnalyticsLabels.USER_LEVEL, level)
  }

  override fun setWalletOrigin(origin: PromotionsModel.WalletOrigin) {
    superProperties.put(AnalyticsLabels.WALLET_ORIGIN, origin)
  }

  override fun setPromoCode(promoCode: PromoCode) {
    superProperties.put(AnalyticsLabels.PROMO_CODE, promoCode)
  }

  fun initialize(): Completable {
    Indicative.launch(context, BuildConfig.INDICATIVE_API_KEY);
    return Single.just(idsRepository.getAndroidId())
      .flatMap { deviceId: String ->
        Single.zip(
          idsRepository.getInstallerPackage(BuildConfig.APPLICATION_ID),
          Single.just(idsRepository.getGamificationLevel()),
          Single.just(hasGms()),
          Single.just(idsRepository.getActiveWalletAddress()),
          promoCodeLocalDataSource.getSavedPromoCode(),
          Single.just(idsRepository.getDeviceInfo())
        )
        { installerPackage: String, level: Int, hasGms: Boolean, walletAddress: String, promoCode: PromoCode, deviceInfo: DeviceInformation ->
          IndicativeInitializeWrapper(installerPackage, level, hasGms, walletAddress, promoCode, deviceInfo)
        }
          .flatMap {
            promotionsRepository.getWalletOrigin(
              it.walletAddress,
              it.promoCode.code
            )
              .doOnSuccess { walletOrigin ->
                setIndicativeSuperProperties(
                  it.installerPackage,
                  it.level, it.walletAddress, it.hasGms, walletOrigin, it.deviceInfo
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


  private fun setIndicativeSuperProperties(
    installerPackage: String,
    userLevel: Int,
    userId: String, hasGms: Boolean, walletOrigin: WalletOrigin, deviceInfo: DeviceInformation
  ) {

    superProperties.put(
      AnalyticsLabels.APTOIDE_PACKAGE,
      BuildConfig.APPLICATION_ID
    )
    superProperties.put(AnalyticsLabels.VERSION_CODE, BuildConfig.VERSION_CODE)
    superProperties.put(
      AnalyticsLabels.ENTRY_POINT, if (installerPackage.isEmpty()) "other" else installerPackage
    )
    superProperties.put(AnalyticsLabels.USER_LEVEL, userLevel)
    superProperties.put(AnalyticsLabels.HAS_GMS, hasGms)
    superProperties.put(AnalyticsLabels.WALLET_ORIGIN, walletOrigin)

    // device information:
    superProperties.put(AnalyticsLabels.OS_VERSION, deviceInfo.osVersion)
    superProperties.put(AnalyticsLabels.BRAND, deviceInfo.brand)
    superProperties.put(AnalyticsLabels.MODEL, deviceInfo.model)
    superProperties.put(AnalyticsLabels.LANGUAGE, deviceInfo.language)
    superProperties.put(AnalyticsLabels.IS_EMULATOR, deviceInfo.isProbablyEmulator)

    if (userId.isNotEmpty()) this.usrId = userId
  }

}

private data class IndicativeInitializeWrapper(
  val installerPackage: String, val level: Int,
  val hasGms: Boolean, val walletAddress: String,
  val promoCode: PromoCode, val deviceInfo: DeviceInformation
)