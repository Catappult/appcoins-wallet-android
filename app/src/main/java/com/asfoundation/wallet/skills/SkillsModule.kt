package com.asfoundation.wallet.skills

import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import cm.aptoide.skills.BuildConfig
import cm.aptoide.skills.SkillsViewModel
import cm.aptoide.skills.api.RoomApi
import cm.aptoide.skills.api.TicketApi
import cm.aptoide.skills.interfaces.EwtObtainer
import cm.aptoide.skills.interfaces.ExternalSkillsPaymentProvider
import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.repository.*
import cm.aptoide.skills.usecase.*
import cm.aptoide.skills.util.EskillsUriParser
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.ewt.EwtAuthenticatorService
import com.asfoundation.wallet.repository.CurrencyConversionService
import com.asfoundation.wallet.ui.iab.RewardsManager
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named


@Module
class SkillsModule {

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
      saveQueueIdToClipboard: SaveQueueIdToClipboard,
      getApplicationInfoUseCase: GetApplicationInfoUseCase,
      getTicketPriceUseCase: GetTicketPriceUseCase,
      getUserBalanceUseCase: GetUserBalanceUseCase,
      sendUserToTopUpFlowUseCase: SendUserToTopUpFlowUseCase
  ): SkillsViewModel {
    return SkillsViewModel(
        walletObtainer, joinQueueUseCase, getTicketUseCase, GET_ROOM_RETRY_MILLIS,
        loginUseCase, cancelUseCase, PublishSubject.create(), payTicketUseCase,
        saveQueueIdToClipboard, getApplicationInfoUseCase, getTicketPriceUseCase,
        getUserBalanceUseCase, sendUserToTopUpFlowUseCase
    )
  }

  @Provides
  fun providesLoginUseCase(ewtObtainer: EwtObtainer,
                           loginRepository: LoginRepository): LoginUseCase {
    return LoginUseCase(ewtObtainer, loginRepository)
  }

  @Provides
  fun providesLoginRepository(roomApi: RoomApi): LoginRepository {
    return LoginRepository(roomApi)
  }

  @Provides
  fun providesGetTicketUseCase(walletAddressObtainer: WalletAddressObtainer,
                               ewtObtainer: EwtObtainer,
                               ticketRepository: TicketRepository): GetTicketUseCase {
    return GetTicketUseCase(walletAddressObtainer, ewtObtainer, ticketRepository)
  }

  @Provides
  fun providesRoomRepository(roomApi: RoomApi): RoomRepository {
    return RoomRepository(roomApi)
  }

  @Provides
  fun providesRoomApi(@Named("default") client: OkHttpClient): RoomApi {
    val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm")
        .create()

    return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(RoomApi::class.java)
  }

  @Provides
  fun providesCreateTicketUseCase(walletAddressObtainer: WalletAddressObtainer,
                                  ewtObtainer: EwtObtainer,
                                  ticketRepository: TicketRepository): JoinQueueUseCase {
    return JoinQueueUseCase(walletAddressObtainer, ewtObtainer, ticketRepository, Schedulers.io())
  }

  @Provides
  fun providesTicketsRepository(@Named("default") client: OkHttpClient,
                                sharedPreferences: SharedPreferences): TicketRepository {
    val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm")
        .create()

    val api = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(TicketApi::class.java)

    return TicketRepository(api, SharedPreferencesTicketLocalStorage(sharedPreferences, gson),
        TicketApiMapper(gson))
  }

  @Provides
  fun providesEskillsUriParser(): EskillsUriParser {
    return EskillsUriParser()
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
  fun providesEWTObtainer(ewtAuthenticatorService: EwtAuthenticatorService): EwtObtainer {
    return DefaultEwtObtainer(ewtAuthenticatorService)
  }

  @Provides
  fun providesPayTicketUseCase(
      externalSkillsPaymentProvider: ExternalSkillsPaymentProvider): PayTicketUseCase {
    return PayTicketUseCase(externalSkillsPaymentProvider)
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
  fun providesSaveQueueIdToClipboard(clipboardManager: ClipboardManager): SaveQueueIdToClipboard {
    return SaveQueueIdToClipboard(clipboardManager)
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

  companion object {
    const val BASE_URL = BuildConfig.BASE_HOST_SKILLS
    const val GET_ROOM_RETRY_MILLIS = 3000L
  }
}