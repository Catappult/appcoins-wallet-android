package com.asfoundation.wallet.eskills.di

import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import cm.aptoide.skills.SkillsViewModel
import cm.aptoide.skills.interfaces.EwtObtainer
import cm.aptoide.skills.interfaces.ExternalAuthenticationProvider
import cm.aptoide.skills.interfaces.ExternalSkillsPaymentProvider
import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.repository.PaymentLocalStorage
import cm.aptoide.skills.repository.SharedPreferencesPaymentLocalStorage
import cm.aptoide.skills.usecase.*
import cm.aptoide.skills.util.EskillsUriParser
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.eskills.DefaultEwtObtainer
import com.asfoundation.wallet.eskills.DefaultWalletAddressObtainer
import com.asfoundation.wallet.eskills.auth.FingerprintAuthenticationProvider
import com.asfoundation.wallet.eskills.payments.AppCoinsCreditsPayment
import com.asfoundation.wallet.eskills.payments.SkillsPaymentRepository
import com.asfoundation.wallet.ewt.EwtAuthenticatorService
import com.asfoundation.wallet.fingerprint.FingerprintPreferencesRepositoryContract
import com.asfoundation.wallet.repository.CurrencyConversionService
import com.asfoundation.wallet.ui.iab.RewardsManager
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import io.reactivex.subjects.PublishSubject


@Module(includes = [UseCaseModule::class, RepositoryModule::class])
class SkillsModule {
  companion object {
    const val GET_ROOM_RETRY_MILLIS = 3000L
  }

  @Provides
  fun providesWalletAddressObtainer(walletService: WalletService): WalletAddressObtainer {
    return DefaultWalletAddressObtainer(walletService)
  }

  @Provides
  fun providesSkillsViewModel(
      walletObtainer: WalletAddressObtainer,
      joinQueueUseCase: JoinQueueUseCase,
      payTicketUseCase: PayTicketUseCase,
      getTicketUseCase: GetTicketUseCase,
      loginUseCase: LoginUseCase,
      cancelUseCase: CancelTicketUseCase,
      saveQueueIdToClipboardUseCase: SaveQueueIdToClipboardUseCase,
      getApplicationInfoUseCase: GetApplicationInfoUseCase,
      getTicketPriceUseCase: GetTicketPriceUseCase,
      getUserBalanceUseCase: GetUserBalanceUseCase,
      sendUserToTopUpFlowUseCase: SendUserToTopUpFlowUseCase,
      hasAuthenticationPermissionUseCase: HasAuthenticationPermissionUseCase,
      getAuthenticationIntentUseCase: GetAuthenticationIntentUseCase,
      cachePaymentUseCase: CachePaymentUseCase,
      getCachedPaymentUseCase: GetCachedPaymentUseCase,
      verificationFlowUseCase: SendUserVerificationFlowUseCase,
  ): SkillsViewModel {
    return SkillsViewModel(
        walletObtainer, joinQueueUseCase, getTicketUseCase, GET_ROOM_RETRY_MILLIS,
        loginUseCase, cancelUseCase, PublishSubject.create(), payTicketUseCase,
        saveQueueIdToClipboardUseCase, getApplicationInfoUseCase, getTicketPriceUseCase,
        getUserBalanceUseCase, sendUserToTopUpFlowUseCase, hasAuthenticationPermissionUseCase,
        getAuthenticationIntentUseCase, cachePaymentUseCase, getCachedPaymentUseCase,
        verificationFlowUseCase
    )
  }

  @Provides
  fun providesEskillsUriParser(): EskillsUriParser {
    return EskillsUriParser()
  }

  @Provides
  fun providesEWTObtainer(ewtAuthenticatorService: EwtAuthenticatorService): EwtObtainer {
    return DefaultEwtObtainer(ewtAuthenticatorService)
  }

  @Provides
  fun providesCreditsSkillsPayment(
      currencyConversionService: CurrencyConversionService,
      currencyFormatUtils: CurrencyFormatUtils,
      rewardsManager: RewardsManager,
      billing: Billing,
      rxSchedulers: RxSchedulers,
      getWalletInfoUseCase: GetWalletInfoUseCase
  ): ExternalSkillsPaymentProvider {
    return SkillsPaymentRepository(currencyConversionService, currencyFormatUtils,
        AppCoinsCreditsPayment(rewardsManager, billing), rxSchedulers, getWalletInfoUseCase)
  }

  @Provides
  fun providesClipboardManager(context: Context): ClipboardManager {
    return context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  }

  @Provides
  fun providesFingerprintAuthenticationProvider(
      fingerprintPreferences: FingerprintPreferencesRepositoryContract): ExternalAuthenticationProvider {
    return FingerprintAuthenticationProvider(fingerprintPreferences)
  }

  @Provides
  fun providesPaymentLocalStorage(sharedPreferences: SharedPreferences): PaymentLocalStorage {
    val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm")
        .create()

    return SharedPreferencesPaymentLocalStorage(sharedPreferences, gson)
  }
}