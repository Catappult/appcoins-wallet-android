package com.asfoundation.wallet.analytics

import android.app.Application
import android.content.Context
import com.appcoins.wallet.commons.Logger
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.identification.IdsRepository
import com.asfoundation.wallet.logging.SentryReceiver
import com.asfoundation.wallet.promo_code.repository.PromoCode
import com.asfoundation.wallet.promo_code.repository.PromoCodeLocalDataSource
import com.asfoundation.wallet.promotions.model.PromotionsModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Completable
import io.reactivex.Single
import io.sentry.Sentry
import io.sentry.android.AndroidSentryClientFactory
import io.sentry.event.Breadcrumb
import io.sentry.event.BreadcrumbBuilder
import io.sentry.event.User
import javax.inject.Inject

class SentryAnalytics @Inject constructor(
  @ApplicationContext private val context: Context, private val idsRepository: IdsRepository,
  private val promotionsRepository: PromotionsRepository,
  private val logger: Logger,
  private val promoCodeLocalDataSource: PromoCodeLocalDataSource
) :
  AnalyticsSetup {

  override fun setUserId(walletAddress: String) {
    val old = Sentry.getContext().user.id
    Sentry.getContext().recordBreadcrumb(
      BreadcrumbBuilder()
        .setType(Breadcrumb.Type.USER)
        .setLevel(Breadcrumb.Level.INFO)
        .setMessage("Changing wallet from $old to $walletAddress")
        .setCategory("wallet")
        .build()
    )
    Sentry.getContext().user = User(walletAddress, null, null, null)
  }

  override fun setGamificationLevel(level: Int) {
    Sentry.getContext().addExtra(AnalyticsLabels.USER_LEVEL, level)
  }

  override fun setWalletOrigin(origin: PromotionsModel.WalletOrigin) {
    Sentry.getContext().addExtra(AnalyticsLabels.WALLET_ORIGIN, origin)
  }

  override fun setPromoCode(promoCode: PromoCode) {
    Sentry.getContext().addExtra(AnalyticsLabels.PROMO_CODE, promoCode)
  }

  fun initialize(): Completable {
    Sentry.init(
      BuildConfig.SENTRY_DSN_KEY,
      AndroidSentryClientFactory(context.applicationContext as Application)
    )
    return Single.zip(
      idsRepository.getInstallerPackage(BuildConfig.APPLICATION_ID),
      Single.just(idsRepository.getGamificationLevel()),
      Single.just(hasGms()),
      Single.just(idsRepository.getActiveWalletAddress()),
      promoCodeLocalDataSource.getSavedPromoCode()
    ) { installerPackage: String, level: Int, hasGms: Boolean, walletAddress: String, promoCode: PromoCode ->
      SentryInitializeWrapper(installerPackage, level, hasGms, walletAddress, promoCode)
    }
      .flatMap {
        promotionsRepository.getWalletOrigin(
          it.walletAddress,
          it.promoCode.code
        )
          .doOnSuccess { walletOrigin ->
            Sentry.getContext().apply {
              user = User(it.walletAddress, null, null, null)
              addExtra(AnalyticsLabels.ENTRY_POINT, it.installerPackage.ifEmpty { "other" })
              addExtra(AnalyticsLabels.USER_LEVEL, it.level)
              addExtra(AnalyticsLabels.HAS_GMS, it.hasGms)
              addExtra(AnalyticsLabels.WALLET_ORIGIN, walletOrigin)
            }
            if (!BuildConfig.DEBUG) {
              logger.addReceiver(SentryReceiver())
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

private data class SentryInitializeWrapper(
  val installerPackage: String, val level: Int,
  val hasGms: Boolean, val walletAddress: String,
  val promoCode: PromoCode
)