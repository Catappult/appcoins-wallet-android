package com.asfoundation.wallet.eskills.di

import android.content.ClipboardManager
import android.content.pm.PackageManager
import cm.aptoide.skills.interfaces.EwtObtainer
import cm.aptoide.skills.interfaces.ExternalAuthenticationProvider
import cm.aptoide.skills.interfaces.ExternalSkillsPaymentProvider
import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.repository.LocalApplicationsRepository
import cm.aptoide.skills.repository.LoginRepository
import cm.aptoide.skills.repository.PaymentLocalStorage
import cm.aptoide.skills.repository.TicketRepository
import cm.aptoide.skills.usecase.*
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers

@Module
class UseCaseModule {

  @Provides
  fun providesLoginUseCase(ewtObtainer: EwtObtainer,
                           loginRepository: LoginRepository): LoginUseCase {
    return LoginUseCase(ewtObtainer, loginRepository)
  }

  @Provides
  fun providesGetTicketUseCase(walletAddressObtainer: WalletAddressObtainer,
                               ewtObtainer: EwtObtainer,
                               ticketRepository: TicketRepository): GetTicketUseCase {
    return GetTicketUseCase(walletAddressObtainer, ewtObtainer, ticketRepository)
  }

  @Provides
  fun providesCreateTicketUseCase(walletAddressObtainer: WalletAddressObtainer,
                                  ewtObtainer: EwtObtainer,
                                  ticketRepository: TicketRepository): JoinQueueUseCase {
    return JoinQueueUseCase(walletAddressObtainer, ewtObtainer, ticketRepository, Schedulers.io())
  }

  @Provides
  fun providesCancelTicketUseCase(
      walletAddressObtainer: WalletAddressObtainer,
      ewtObtainer: EwtObtainer,
      ticketRepository: TicketRepository
  ): CancelTicketUseCase {
    return CancelTicketUseCase(walletAddressObtainer, ewtObtainer, ticketRepository)
  }

  @Provides
  fun providesPayTicketUseCase(
      externalSkillsPaymentProvider: ExternalSkillsPaymentProvider): PayTicketUseCase {
    return PayTicketUseCase(externalSkillsPaymentProvider)
  }

  @Provides
  fun providesSaveQueueIdToClipboardUseCase(
      clipboardManager: ClipboardManager): SaveQueueIdToClipboardUseCase {
    return SaveQueueIdToClipboardUseCase(clipboardManager)
  }

  @Provides
  fun providesGetApplicationUseCase(packageManager: PackageManager): GetApplicationInfoUseCase {
    return GetApplicationInfoUseCase(LocalApplicationsRepository(packageManager))
  }

  @Provides
  fun providesGetTicketPriceUseCase(
      externalSkillsPaymentProvider: ExternalSkillsPaymentProvider): GetTicketPriceUseCase {
    return GetTicketPriceUseCase(externalSkillsPaymentProvider)
  }

  @Provides
  fun providesGetUserBalanceUseCase(
      externalSkillsPaymentProvider: ExternalSkillsPaymentProvider): GetUserBalanceUseCase {
    return GetUserBalanceUseCase(externalSkillsPaymentProvider)
  }

  @Provides
  fun providesSendUserToTopUpFlowUseCase(
      externalSkillsPaymentProvider: ExternalSkillsPaymentProvider): SendUserToTopUpFlowUseCase {
    return SendUserToTopUpFlowUseCase(externalSkillsPaymentProvider)
  }

  @Provides
  fun providesSendUserToVerificationFlowUseCase(
      externalSkillsPaymentProvider: ExternalSkillsPaymentProvider): SendUserVerificationFlowUseCase {
    return SendUserVerificationFlowUseCase(externalSkillsPaymentProvider)
  }

  @Provides
  fun providesHasAuthenticationPermissionUseCase(
      externalAuthenticationProvider: ExternalAuthenticationProvider): HasAuthenticationPermissionUseCase {
    return HasAuthenticationPermissionUseCase(externalAuthenticationProvider)
  }

  @Provides
  fun providesGetAuthenticationIntentUseCase(
      externalAuthenticationProvider: ExternalAuthenticationProvider): GetAuthenticationIntentUseCase {
    return GetAuthenticationIntentUseCase(externalAuthenticationProvider)
  }


  @Provides
  fun providesCachePaymentUseCase(paymentLocalStorage: PaymentLocalStorage): CachePaymentUseCase {
    return CachePaymentUseCase(paymentLocalStorage)
  }

  @Provides
  fun providesGetCachedPaymentUseCase(
      paymentLocalStorage: PaymentLocalStorage): GetCachedPaymentUseCase {
    return GetCachedPaymentUseCase(paymentLocalStorage)
  }
}
