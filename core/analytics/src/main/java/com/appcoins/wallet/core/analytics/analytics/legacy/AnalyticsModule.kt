package com.appcoins.wallet.core.analytics.analytics.legacy

import android.content.Context
import cm.aptoide.analytics.AnalyticsManager
import com.appcoins.wallet.core.analytics.analytics.BackendEventLogger
import com.appcoins.wallet.core.analytics.analytics.GAEventLogger
import com.appcoins.wallet.core.analytics.analytics.HttpClientKnockLogger
import com.appcoins.wallet.core.analytics.analytics.IndicativeAnalytics
import com.appcoins.wallet.core.analytics.analytics.IndicativeEventLogger
import com.appcoins.wallet.core.analytics.analytics.KeysNormalizer
import com.appcoins.wallet.core.analytics.analytics.LogcatAnalyticsLogger
import com.appcoins.wallet.core.analytics.analytics.SentryEventLogger
import com.appcoins.wallet.core.analytics.analytics.compatible_apps.CompatibleAppsAnalytics.Companion.WALLET_APP_ACTIVE_PROMOTION_CLICK
import com.appcoins.wallet.core.analytics.analytics.email.EmailAnalytics.Companion.WALLET_APP_EMAIL_SUBMITTED
import com.appcoins.wallet.core.analytics.analytics.email.EmailAnalytics.Companion.WALLET_APP_HOME_SCREEN_CLICK
import com.appcoins.wallet.core.analytics.analytics.legacy.ChallengeRewardAnalytics.Companion.CHALLENGE_REWARD_EVENT
import com.appcoins.wallet.core.analytics.analytics.manage_cards.ManageCardsAnalytics.Companion.MANAGE_PAYMENT_CARDS
import com.appcoins.wallet.core.analytics.analytics.manage_cards.ManageCardsAnalytics.Companion.WALLET_APP_ADDED_CARD_CONCLUSION_IMPRESSION
import com.appcoins.wallet.core.analytics.analytics.manage_cards.ManageCardsAnalytics.Companion.WALLET_APP_ADD_NEW_CARD_DETAILS_CLICK
import com.appcoins.wallet.core.analytics.analytics.manage_cards.ManageCardsAnalytics.Companion.WALLET_APP_ADD_NEW_CARD_DETAILS_IMPRESSION
import com.appcoins.wallet.core.analytics.analytics.manage_cards.ManageCardsAnalytics.Companion.WALLET_APP_REMOVED_CARD_CONCLUSION_IMPRESSION
import com.appcoins.wallet.core.analytics.analytics.manage_cards.ManageCardsAnalytics.Companion.WALLET_APP_REMOVE_SAVED_CARD_PROMPT_CLICK
import com.appcoins.wallet.core.network.analytics.api.AnalyticsApi
import com.appcoins.wallet.core.network.base.annotations.DefaultHttpClient
import com.appcoins.wallet.sharedpreferences.AppStartPreferencesDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AnalyticsModule {

  @Singleton
  @Provides
  @Named("bi_event_list")
  fun provideBiEventList() =
    listOf(
      BillingAnalytics.PURCHASE_DETAILS,
      BillingAnalytics.PAYMENT_METHOD_DETAILS,
      BillingAnalytics.PAYMENT,
    )

  @Singleton
  @Provides
  @Named("indicative_event_list")
  fun provideIndicativeEventList() =
    listOf(
      FIRST_LAUNCH, HomeAnalytics.WALLET_HOME_INTERACTION_EVENT,
      BillingAnalytics.WALLET_PRESELECTED_PAYMENT_METHOD, BillingAnalytics.WALLET_PAYMENT_METHOD,
      BillingAnalytics.WALLET_PAYMENT_CONFIRMATION, BillingAnalytics.WALLET_PAYMENT_CONCLUSION,
      BillingAnalytics.WALLET_PAYMENT_START, BillingAnalytics.WALLET_PAYPAL_URL,
      BillingAnalytics.WALLET_PAYMENT_METHOD_DETAILS, BillingAnalytics.WALLET_PAYMENT_BILLING,
      WALLET_TOP_UP_START, WALLET_TOP_UP_SELECTION,
      WALLET_TOP_UP_CONFIRMATION, WALLET_TOP_UP_CONCLUSION,
      WALLET_TOP_UP_PAYPAL_URL, WALLET_TOP_UP_BILLING,
      WalletsAnalytics.WALLET_BACKUP_CREATE, WalletsAnalytics.WALLET_BACKUP_INFO,
      WalletsAnalytics.WALLET_BACKUP_CONFIRMATION, WalletsAnalytics.WALLET_BACKUP_CONCLUSION,
      WalletsAnalytics.WALLET_IMPORT_RESTORE,
      WalletsAnalytics.WALLET_MY_WALLETS_INTERACTION_EVENT,
      WalletsAnalytics.WALLET_PASSWORD_RESTORE, PageViewAnalytics.WALLET_PAGE_VIEW,
      TOPUP_DEFAULT_VALUE_PARTICIPATING_EVENT,
      WALLET_RATING_WELCOME_EVENT, WALLET_RATING_POSITIVE_EVENT,
      WALLET_RATING_NEGATIVE_EVENT, WALLET_RATING_FINISH_EVENT,
      START_EVENT, INSERT_CARD_EVENT,
      REQUEST_CONCLUSION_EVENT, CONFIRM_EVENT,
      CONCLUSION_EVENT,
      WALLET_PAYMENT_LOADING_TOTAL,
      WALLET_PAYMENT_LOADING_STEP,
      WALLET_PAYMENT_PROCESSING_TOTAL,
      WALLET_3DS_START,
      WALLET_3DS_CANCEL,
      WALLET_3DS_ERROR,
      WALLET_CALLOUT_PROMOTIONS_CLICK,
      EVENT_WALLET_PAYMENT_CONCLUSION_NAVIGATION,
      ONBOARDING_PAYMENT,
      WALLET_ONBOARDING_RECOVER_WEB,
      CHALLENGE_REWARD_EVENT,
      WALLET_APP_ACTIVE_PROMOTION_CLICK,
      WALLET_APP_REWARDS_SCREEN_IMPRESSION,
      WALLET_APP_REWARDS_SCREEN_CLICK,
      WALLET_APP_SUBMIT_NEW_PROMO_CODE_IMPRESSION,
      WALLET_APP_SUBMIT_NEW_PROMO_CODE_CLICK,
      WALLET_APP_SUBMIT_PROMO_CODE_SUCCESS_IMPRESSION,
      WALLET_APP_SUBMIT_PROMO_CODE_SUCCESS_CLICK,
      WALLET_APP_SUBMIT_PROMO_CODE_ERROR_IMPRESSION,
      WALLET_APP_SUBMIT_PROMO_CODE_ERROR_CLICK,
      WALLET_APP_REPLACE_PROMO_CODE_IMPRESSION,
      WALLET_APP_REPLACE_PROMO_CODE_CLICK,
      WALLET_APP_ACTIVE_PROMOTION_CLICK,
      WALLET_APP_TOP_UP_IMPRESSION,
      WALLET_APP_TOP_UP_CHANGE_CARD_PROMPT_CLICK,
      WALLET_PAYMENT_START_CARD_LIST,
      WALLET_APP_ADD_NEW_CARD_DETAILS_IMPRESSION,
      WALLET_APP_ADD_NEW_CARD_DETAILS_CLICK,
      WALLET_APP_ADDED_CARD_CONCLUSION_IMPRESSION,
      MANAGE_PAYMENT_CARDS,
      WALLET_APP_REMOVE_SAVED_CARD_PROMPT_CLICK,
      WALLET_APP_REMOVED_CARD_CONCLUSION_IMPRESSION,
      WALLET_APP_MANAGE_PAYMENT_CARDS_IMPRESSION,
      WALLET_APP_SETTINGS_CLICK,
      WALLET_APP_TOP_UP_CLICK,
      WALLET_APP_EMAIL_SUBMITTED,
      WALLET_APP_HOME_SCREEN_CLICK,
      WALLET_APP_CLICK,
    )

  @Singleton
  @Provides
  @Named("sentry_event_list")
  fun provideSentryEventList() =
    listOf(
      FIRST_LAUNCH, HomeAnalytics.WALLET_HOME_INTERACTION_EVENT,
      BillingAnalytics.WALLET_PRESELECTED_PAYMENT_METHOD, BillingAnalytics.WALLET_PAYMENT_METHOD,
      BillingAnalytics.WALLET_PAYMENT_CONFIRMATION, BillingAnalytics.WALLET_PAYMENT_CONCLUSION,
      BillingAnalytics.WALLET_PAYMENT_START, BillingAnalytics.WALLET_PAYPAL_URL,
      BillingAnalytics.WALLET_PAYMENT_METHOD_DETAILS, BillingAnalytics.WALLET_PAYMENT_BILLING,
      WALLET_TOP_UP_START, WALLET_TOP_UP_SELECTION,
      WALLET_TOP_UP_CONFIRMATION, WALLET_TOP_UP_CONCLUSION,
      WALLET_TOP_UP_PAYPAL_URL, WALLET_TOP_UP_BILLING,
      WalletsAnalytics.WALLET_BACKUP_CREATE, WalletsAnalytics.WALLET_BACKUP_INFO,
      WalletsAnalytics.WALLET_BACKUP_CONFIRMATION, WalletsAnalytics.WALLET_BACKUP_CONCLUSION,
      WalletsAnalytics.WALLET_IMPORT_RESTORE,
      WalletsAnalytics.WALLET_MY_WALLETS_INTERACTION_EVENT,
      WalletsAnalytics.WALLET_PASSWORD_RESTORE, PageViewAnalytics.WALLET_PAGE_VIEW,
      TOPUP_DEFAULT_VALUE_PARTICIPATING_EVENT,
      WALLET_RATING_WELCOME_EVENT, WALLET_RATING_POSITIVE_EVENT,
      WALLET_RATING_NEGATIVE_EVENT, WALLET_RATING_FINISH_EVENT,
      START_EVENT, INSERT_CARD_EVENT,
      REQUEST_CONCLUSION_EVENT, CONFIRM_EVENT,
      CONCLUSION_EVENT,
      WALLET_PAYMENT_LOADING_TOTAL,
      WALLET_PAYMENT_LOADING_STEP,
      WALLET_PAYMENT_PROCESSING_TOTAL,
      WALLET_3DS_START,
      WALLET_3DS_CANCEL,
      WALLET_3DS_ERROR,
      WALLET_CALLOUT_PROMOTIONS_CLICK,
      EVENT_WALLET_PAYMENT_CONCLUSION_NAVIGATION,
      ONBOARDING_PAYMENT,
      WALLET_ONBOARDING_RECOVER_WEB,
      CHALLENGE_REWARD_EVENT,
      WALLET_APP_ACTIVE_PROMOTION_CLICK,
      WALLET_APP_REWARDS_SCREEN_IMPRESSION,
      WALLET_APP_REWARDS_SCREEN_CLICK,
      WALLET_APP_SUBMIT_NEW_PROMO_CODE_IMPRESSION,
      WALLET_APP_SUBMIT_NEW_PROMO_CODE_CLICK,
      WALLET_APP_SUBMIT_PROMO_CODE_SUCCESS_IMPRESSION,
      WALLET_APP_SUBMIT_PROMO_CODE_SUCCESS_CLICK,
      WALLET_APP_SUBMIT_PROMO_CODE_ERROR_IMPRESSION,
      WALLET_APP_SUBMIT_PROMO_CODE_ERROR_CLICK,
      WALLET_APP_REPLACE_PROMO_CODE_IMPRESSION,
      WALLET_APP_REPLACE_PROMO_CODE_CLICK,
      WALLET_APP_TOP_UP_IMPRESSION,
      WALLET_APP_TOP_UP_CHANGE_CARD_PROMPT_CLICK,
      WALLET_PAYMENT_START_CARD_LIST,
      WALLET_APP_ADD_NEW_CARD_DETAILS_IMPRESSION,
      WALLET_APP_ADD_NEW_CARD_DETAILS_CLICK,
      WALLET_APP_ADDED_CARD_CONCLUSION_IMPRESSION,
      MANAGE_PAYMENT_CARDS,
      WALLET_APP_REMOVE_SAVED_CARD_PROMPT_CLICK,
      WALLET_APP_REMOVED_CARD_CONCLUSION_IMPRESSION,
      WALLET_APP_MANAGE_PAYMENT_CARDS_IMPRESSION,
      WALLET_APP_SETTINGS_CLICK,
      WALLET_APP_TOP_UP_CLICK,
      WALLET_APP_EMAIL_SUBMITTED,
      WALLET_APP_HOME_SCREEN_CLICK,
      WALLET_APP_CLICK
    )

  @Singleton
  @Provides
  fun provideAnalyticsManager(
    @DefaultHttpClient okHttpClient: OkHttpClient, api: AnalyticsApi,
    @Named("bi_event_list") biEventList: List<String>,
    @Named("indicative_event_list") indicativeEventList: List<String>,
    @Named("sentry_event_list") sentryEventList: List<String>,
    indicativeAnalytics: IndicativeAnalytics,
    appStartPreferencesDataSource: AppStartPreferencesDataSource,
    @ApplicationContext context: Context
  ): AnalyticsManager {
    return AnalyticsManager.Builder()
      .addLogger(BackendEventLogger(api, VERSION_CODE, APPLICATION_ID), biEventList)
      .addLogger(
        IndicativeEventLogger(indicativeAnalytics, appStartPreferencesDataSource),
        indicativeEventList
      )
      .addLogger(
        GAEventLogger(indicativeAnalytics, appStartPreferencesDataSource, context),
        indicativeEventList
      )
      .addLogger(SentryEventLogger(), sentryEventList)
      .setAnalyticsNormalizer(KeysNormalizer())
      .setDebugLogger(LogcatAnalyticsLogger())
      .setKnockLogger(HttpClientKnockLogger(okHttpClient))
      .build()
  }

  companion object {
    const val FIRST_LAUNCH = "wallet_first_launch" //AppStartProb
    const val WALLET_TOP_UP_START = "wallet_top_up_start" //TopUpAnalytics
    const val WALLET_TOP_UP_SELECTION = "wallet_top_up_selection"
    const val WALLET_TOP_UP_CONFIRMATION = "wallet_top_up_confirmation"
    const val WALLET_TOP_UP_CONCLUSION = "wallet_top_up_conclusion"
    const val WALLET_TOP_UP_PAYPAL_URL = "wallet_top_up_conclusion_paypal"
    const val WALLET_TOP_UP_BILLING = "wallet_top_up_billing"
    const val WALLET_APP_MANAGE_PAYMENT_CARDS_IMPRESSION =
      "wallet_app_manage_payment_cards_impression"
    const val WALLET_APP_SETTINGS_CLICK = "wallet_app_settings_click"
    const val WALLET_APP_TOP_UP_CLICK = "wallet_app_top_up_click"
    const val WALLET_APP_TOP_UP_IMPRESSION = "wallet_app_top_up_impression"
    const val WALLET_APP_TOP_UP_CHANGE_CARD_PROMPT_CLICK =
      "wallet_app_top_up_change_card_prompt_click"
    const val WALLET_PAYMENT_START_CARD_LIST = "wallet_payment_start_change_card_prompt_click"
    const val TOPUP_DEFAULT_VALUE_PARTICIPATING_EVENT =
      "wallet_top_default_value_ab_testing_participating"  //TopUpDefaultValueProb
    const val WALLET_RATING_WELCOME_EVENT = "wallet_rating_welcome" //RatingAnalytics
    const val WALLET_RATING_POSITIVE_EVENT = "wallet_rating_positive"
    const val WALLET_RATING_NEGATIVE_EVENT = "wallet_rating_negative"
    const val WALLET_RATING_FINISH_EVENT = "wallet_rating_finish"
    const val START_EVENT = "wallet_verify_start" //VerificationAnalytics
    const val INSERT_CARD_EVENT = "wallet_verify_insert_card"
    const val REQUEST_CONCLUSION_EVENT = "wallet_verify_request_conclusion"
    const val CONFIRM_EVENT = "wallet_verify_confirm"
    const val CONCLUSION_EVENT = "wallet_verify_conclusion"
    const val WALLET_PAYMENT_LOADING_TOTAL = "wallet_payment_loading_total" //PaymentMethodAnalytics
    const val WALLET_PAYMENT_LOADING_STEP = "wallet_payment_loading_step"
    const val WALLET_PAYMENT_PROCESSING_TOTAL = "wallet_payment_processing_total"
    const val WALLET_3DS_START = "wallet_3ds_start"
    const val WALLET_3DS_CANCEL = "wallet_3ds_cancel"
    const val WALLET_3DS_ERROR = "wallet_3ds_error"
    const val WALLET_CALLOUT_PROMOTIONS_CLICK = "wallet_callout_promotions_click" //NavBarAnalytics
    const val EVENT_WALLET_PAYMENT_CONCLUSION_NAVIGATION =
      "wallet_payment_conclusion_navigation" //OnBoardingPaymentsEvent
    private const val WALLET_APP_REWARDS_SCREEN_IMPRESSION = "wallet_app_rewards_screen_impression"
    private const val WALLET_APP_REWARDS_SCREEN_CLICK = "wallet_app_rewards_screen_click"
    private const val WALLET_APP_SUBMIT_NEW_PROMO_CODE_IMPRESSION =
      "wallet_app_submit_new_promo_code_impression"
    private const val WALLET_APP_SUBMIT_NEW_PROMO_CODE_CLICK =
      "wallet_app_submit_new_promo_code_click"
    private const val WALLET_APP_SUBMIT_PROMO_CODE_SUCCESS_IMPRESSION =
      "wallet_app_submit_promo_code_success_impression"
    private const val WALLET_APP_SUBMIT_PROMO_CODE_SUCCESS_CLICK =
      "wallet_app_submit_promo_code_success_click"
    private const val WALLET_APP_SUBMIT_PROMO_CODE_ERROR_IMPRESSION =
      "wallet_app_submit_promo_code_error_impression"
    private const val WALLET_APP_SUBMIT_PROMO_CODE_ERROR_CLICK =
      "wallet_app_submit_promo_code_error_click"
    private const val WALLET_APP_REPLACE_PROMO_CODE_IMPRESSION =
      "wallet_app_replace_promo_code_impression"
    private const val WALLET_APP_REPLACE_PROMO_CODE_CLICK = "wallet_app_replace_promo_code_click"
    const val ONBOARDING_PAYMENT = "onboarding_payment"
    const val WALLET_ONBOARDING_RECOVER_WEB = "wallet_onboarding_recover_web"
    const val VERSION_CODE = 259 //com.asf.wallet.BuildConfig
    const val APPLICATION_ID = "com.appcoins.wallet.dev"
    const val WALLET_APP_CLICK = "wallet_app_click"
  }

}
