package com.asfoundation.wallet.di

import android.content.Context
import cm.aptoide.analytics.AnalyticsManager
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.asfoundation.wallet.abtesting.experiments.balancewallets.BalanceWalletsAnalytics
import com.asfoundation.wallet.advertise.PoaAnalyticsController
import com.asfoundation.wallet.analytics.*
import com.asfoundation.wallet.analytics.gamification.GamificationAnalytics
import com.asfoundation.wallet.billing.analytics.*
import com.asfoundation.wallet.identification.IdsRepository
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.rating.RatingAnalytics
import com.asfoundation.wallet.topup.TopUpAnalytics
import com.asfoundation.wallet.transactions.TransactionsAnalytics
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import com.asfoundation.wallet.ui.iab.localpayments.LocalPaymentAnalytics
import com.asfoundation.wallet.verification.VerificationAnalytics
import com.facebook.appevents.AppEventsLogger
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Named
import javax.inject.Singleton

@Module
class AnalyticsModule {

  @Provides
  fun provideLocalPaymentAnalytics(billingAnalytics: BillingAnalytics): LocalPaymentAnalytics {
    return LocalPaymentAnalytics(billingAnalytics)
  }

  @Singleton
  @Provides
  fun providesPageViewAnalytics(analyticsManager: AnalyticsManager): PageViewAnalytics {
    return PageViewAnalytics(analyticsManager)
  }

  @Singleton
  @Provides
  @Named("bi_event_list")
  fun provideBiEventList() = listOf(
      BillingAnalytics.PURCHASE_DETAILS,
      BillingAnalytics.PAYMENT_METHOD_DETAILS,
      BillingAnalytics.PAYMENT,
      PoaAnalytics.POA_STARTED,
      PoaAnalytics.POA_COMPLETED)

  @Singleton
  @Provides
  @Named("facebook_event_list")
  fun provideFacebookEventList() = listOf(
      BillingAnalytics.PURCHASE_DETAILS,
      BillingAnalytics.PAYMENT_METHOD_DETAILS,
      BillingAnalytics.PAYMENT,
      BillingAnalytics.REVENUE,
      PoaAnalytics.POA_STARTED,
      PoaAnalytics.POA_COMPLETED,
      TransactionsAnalytics.OPEN_APPLICATION,
      GamificationAnalytics.GAMIFICATION,
      GamificationAnalytics.GAMIFICATION_MORE_INFO
  )

  @Singleton
  @Provides
  @Named("rakam_event_list")
  fun provideRakamEventList() = listOf(
      LaunchAnalytics.FIRST_LAUNCH,
      TransactionsAnalytics.WALLET_HOME_INTERACTION_EVENT,
      BillingAnalytics.RAKAM_PRESELECTED_PAYMENT_METHOD,
      BillingAnalytics.RAKAM_PAYMENT_METHOD,
      BillingAnalytics.RAKAM_PAYMENT_CONFIRMATION,
      BillingAnalytics.RAKAM_PAYMENT_CONCLUSION,
      BillingAnalytics.RAKAM_PAYMENT_START,
      BillingAnalytics.RAKAM_PAYPAL_URL,
      BillingAnalytics.RAKAM_PAYMENT_METHOD_DETAILS,
      BillingAnalytics.RAKAM_PAYMENT_BILLING,
      TopUpAnalytics.WALLET_TOP_UP_START,
      TopUpAnalytics.WALLET_TOP_UP_SELECTION,
      TopUpAnalytics.WALLET_TOP_UP_CONFIRMATION,
      TopUpAnalytics.WALLET_TOP_UP_CONCLUSION,
      TopUpAnalytics.WALLET_TOP_UP_PAYPAL_URL,
      TopUpAnalytics.RAKAM_TOP_UP_BILLING,
      PoaAnalytics.RAKAM_POA_EVENT,
      WalletsAnalytics.WALLET_CREATE_BACKUP,
      WalletsAnalytics.WALLET_SAVE_BACKUP,
      WalletsAnalytics.WALLET_CONFIRMATION_BACKUP,
      WalletsAnalytics.WALLET_SAVE_FILE,
      WalletsAnalytics.WALLET_IMPORT_RESTORE,
      WalletsAnalytics.WALLET_PASSWORD_RESTORE,
      PageViewAnalytics.WALLET_PAGE_VIEW,
      BalanceWalletsAnalytics.WAL_78_BALANCE_VS_MYWALLETS_PARTICIPATING_EVENT,
      BalanceWalletsAnalytics.WAL_78_BALANCE_VS_MYWALLETS_CONVERSION_EVENT,
      RatingAnalytics.WALLET_RATING_WELCOME_EVENT,
      RatingAnalytics.WALLET_RATING_POSITIVE_EVENT,
      RatingAnalytics.WALLET_RATING_NEGATIVE_EVENT,
      RatingAnalytics.WALLET_RATING_FINISH_EVENT,
      VerificationAnalytics.START_EVENT,
      VerificationAnalytics.INSERT_CARD_EVENT,
      VerificationAnalytics.REQUEST_CONCLUSION_EVENT,
      VerificationAnalytics.CONFIRM_EVENT,
      VerificationAnalytics.CONCLUSION_EVENT
  )

  @Singleton
  @Provides
  @Named("amplitude_event_list")
  fun provideAmplitudeEventList() = listOf(
      BillingAnalytics.RAKAM_PRESELECTED_PAYMENT_METHOD,
      BillingAnalytics.RAKAM_PAYMENT_METHOD,
      BillingAnalytics.RAKAM_PAYMENT_CONFIRMATION,
      BillingAnalytics.RAKAM_PAYMENT_CONCLUSION,
      BillingAnalytics.RAKAM_PAYMENT_START,
      BillingAnalytics.RAKAM_PAYPAL_URL,
      TopUpAnalytics.WALLET_TOP_UP_START,
      TopUpAnalytics.WALLET_TOP_UP_SELECTION,
      TopUpAnalytics.WALLET_TOP_UP_CONFIRMATION,
      TopUpAnalytics.WALLET_TOP_UP_CONCLUSION,
      TopUpAnalytics.WALLET_TOP_UP_PAYPAL_URL,
      PoaAnalytics.RAKAM_POA_EVENT,
      WalletsAnalytics.WALLET_CREATE_BACKUP,
      WalletsAnalytics.WALLET_SAVE_BACKUP,
      WalletsAnalytics.WALLET_CONFIRMATION_BACKUP,
      WalletsAnalytics.WALLET_SAVE_FILE,
      WalletsAnalytics.WALLET_IMPORT_RESTORE,
      WalletsAnalytics.WALLET_PASSWORD_RESTORE,
      PageViewAnalytics.WALLET_PAGE_VIEW
  )

  @Singleton
  @Provides
  fun provideAnalyticsManager(@Named("default") okHttpClient: OkHttpClient, api: AnalyticsAPI,
                              context: Context, @Named("bi_event_list") biEventList: List<String>,
                              @Named("facebook_event_list") facebookEventList: List<String>,
                              @Named("rakam_event_list") rakamEventList: List<String>,
                              @Named("amplitude_event_list")
                              amplitudeEventList: List<String>): AnalyticsManager {
    return AnalyticsManager.Builder()
        .addLogger(BackendEventLogger(api), biEventList)
        .addLogger(FacebookEventLogger(AppEventsLogger.newLogger(context)), facebookEventList)
        .addLogger(RakamEventLogger(), rakamEventList)
        .addLogger(AmplitudeEventLogger(), amplitudeEventList)
        .setAnalyticsNormalizer(KeysNormalizer())
        .setDebugLogger(LogcatAnalyticsLogger())
        .setKnockLogger(HttpClientKnockLogger(okHttpClient))
        .build()
  }

  @Singleton
  @Provides
  fun provideWalletEventSender(analytics: AnalyticsManager): WalletsEventSender =
      WalletsAnalytics(analytics)

  @Singleton
  @Provides
  fun provideBillingAnalytics(analytics: AnalyticsManager) = BillingAnalytics(analytics)

  @Singleton
  @Provides
  fun providePoAAnalytics(analytics: AnalyticsManager) = PoaAnalytics(analytics)

  @Singleton
  @Provides
  fun providesPoaAnalyticsController() = PoaAnalyticsController(CopyOnWriteArrayList())

  @Singleton
  @Provides
  fun providesTransactionsAnalytics(analytics: AnalyticsManager,
                                    balanceWalletsAnalytics: BalanceWalletsAnalytics) =
      TransactionsAnalytics(analytics, balanceWalletsAnalytics)

  @Singleton
  @Provides
  fun provideGamificationAnalytics(analytics: AnalyticsManager) = GamificationAnalytics(analytics)

  @Singleton
  @Provides
  fun provideRakamAnalyticsSetup(context: Context, idsRepository: IdsRepository,
                                 promotionsRepository: PromotionsRepository,
                                 logger: Logger): RakamAnalytics {
    return RakamAnalytics(context, idsRepository, promotionsRepository, logger)
  }

  @Singleton
  @Provides
  fun provideAmplitudeAnalytics(context: Context,
                                idsRepository: IdsRepository): AmplitudeAnalytics {
    return AmplitudeAnalytics(context, idsRepository)
  }

  @Singleton
  @Provides
  fun provideLaunchAnalytics(analyticsManager: AnalyticsManager) = LaunchAnalytics(analyticsManager)

  @Singleton
  @Provides
  fun provideTopUpAnalytics(analyticsManager: AnalyticsManager) = TopUpAnalytics(analyticsManager)

  @Provides
  fun providePaymentMethodsAnalytics(billingAnalytics: BillingAnalytics,
                                     rakamAnalytics: RakamAnalytics,
                                     amplitudeAnalytics: AmplitudeAnalytics): PaymentMethodsAnalytics {
    return PaymentMethodsAnalytics(billingAnalytics, rakamAnalytics, amplitudeAnalytics)
  }

  @Singleton
  @Provides
  fun providesBalanceWalletsAnalytics(analytics: AnalyticsManager): BalanceWalletsAnalytics {
    return BalanceWalletsAnalytics(analytics)
  }

  @Singleton
  @Provides
  fun providesRatingAnalytics(analyticsManager: AnalyticsManager) =
      RatingAnalytics(analyticsManager)

  @Singleton
  @Provides
  fun providesVerificationAnalytics(analytics: AnalyticsManager): VerificationAnalytics {
    return VerificationAnalytics(analytics)
  }
}
