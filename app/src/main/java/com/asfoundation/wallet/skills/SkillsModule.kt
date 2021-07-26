package com.asfoundation.wallet.skills

import cm.aptoide.skills.BuildConfig
import cm.aptoide.skills.SkillsViewModel
import cm.aptoide.skills.api.RoomApi
import cm.aptoide.skills.api.TicketApi
import cm.aptoide.skills.interfaces.EwtObtainer
import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.repository.LoginRepository
import cm.aptoide.skills.repository.RoomRepository
import cm.aptoide.skills.repository.TicketRepository
import cm.aptoide.skills.usecase.*
import cm.aptoide.skills.util.EskillsUriParser
import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.ewt.EwtAuthenticatorService
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
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
  fun providesSkillsViewModel(createTicketUseCase: CreateTicketUseCase,
                              payTicketUseCase: PayTicketUseCase,
                              getTicketUseCase: GetTicketUseCase,
                              loginUseCase: LoginUseCase,
                              cancelUseCase: CancelTicketUseCase): SkillsViewModel {
    return SkillsViewModel(
        createTicketUseCase, payTicketUseCase, getTicketUseCase, GET_ROOM_RETRY_MILLIS,
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
  fun providesRoomRepository(roomApi: RoomApi): RoomRepository {
    return RoomRepository(roomApi)
  }

  @Provides
  fun providesRoomApi(@Named("default") client: OkHttpClient): RoomApi {
    val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm")
        .create()

    return Retrofit.Builder()
        .baseUrl(ENDPOINT)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(RoomApi::class.java)
  }

  @Provides
  fun providesPayTicketUseCase(ticketRepository: TicketRepository): PayTicketUseCase {
    return PayTicketUseCase(ticketRepository)
  }

  @Provides
  fun providesCreateTicketUseCase(walletAddressObtainer: WalletAddressObtainer,
                                  ewtObtainer: EwtObtainer,
                                  ticketRepository: TicketRepository): CreateTicketUseCase {
    return CreateTicketUseCase(walletAddressObtainer, ewtObtainer, ticketRepository, Schedulers.io())
  }

  @Provides
  fun providesTicketsRepository(@Named("default") client: OkHttpClient): TicketRepository {
    val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm")
        .create()

    val api = Retrofit.Builder()
        .baseUrl(ENDPOINT)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(TicketApi::class.java)

    return TicketRepository(api)
  }

  @Provides
  fun providesEWTObtainer(ewtAuthenticatorService: EwtAuthenticatorService): EwtObtainer {
    return DefaultEwtObtainer(ewtAuthenticatorService)
  }

  @Provides
  fun providesEwtAuthService(walletService: WalletService): EwtAuthenticatorService {
    val headerJson = JsonObject()
    headerJson.addProperty("typ", "EWT")
    return EwtAuthenticatorService(walletService, headerJson.toString())
  }

  @Provides
  fun providesEskillsUriParser(): EskillsUriParser {
    return EskillsUriParser()
  }

  @Provides
  fun providesCancelUseCase(walletAddressObtainer: WalletAddressObtainer,
                            ewtObtainer: EwtObtainer,
                            ticketRepository: TicketRepository): CancelTicketUseCase {
    return CancelTicketUseCase(walletAddressObtainer, ewtObtainer, ticketRepository)
  }

  companion object {
    const val ENDPOINT = BuildConfig.BASE_HOST_SKILLS
    const val GET_ROOM_RETRY_MILLIS = 1000L
  }
}