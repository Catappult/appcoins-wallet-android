package com.asfoundation.wallet.skills

import android.content.SharedPreferences
import cm.aptoide.skills.BuildConfig
import cm.aptoide.skills.SkillsViewModel
import cm.aptoide.skills.api.RoomApi
import cm.aptoide.skills.api.TicketApi
import cm.aptoide.skills.interfaces.EwtObtainer
import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.repository.*
import cm.aptoide.skills.usecase.*
import cm.aptoide.skills.util.EskillsUriParser
import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.ewt.EwtAuthenticatorService
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named

@InstallIn(FragmentComponent::class)
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
      payTicketUseCase: SkillsNavigator,
      getTicketUseCase: GetTicketUseCase,
      loginUseCase: LoginUseCase,
      cancelUseCase: CancelTicketUseCase
  ): SkillsViewModel {
    return SkillsViewModel(
        walletObtainer, joinQueueUseCase, payTicketUseCase, getTicketUseCase,
        GET_ROOM_RETRY_MILLIS,
        loginUseCase, cancelUseCase, PublishSubject.create()
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
  fun providesPayTicketUseCase(): SkillsNavigator {
    return SkillsNavigator()
  }

  @Provides
  fun providesCreateTicketUseCase(walletAddressObtainer: WalletAddressObtainer,
                                  ewtObtainer: EwtObtainer,
                                  ticketRepository: TicketRepository): JoinQueueUseCase {
    return JoinQueueUseCase(walletAddressObtainer, ewtObtainer, ticketRepository, Schedulers.io())
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

  companion object {
    const val BASE_URL = BuildConfig.BASE_HOST_SKILLS
    const val GET_ROOM_RETRY_MILLIS = 3000L
  }
}