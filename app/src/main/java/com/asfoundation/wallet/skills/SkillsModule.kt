package com.asfoundation.wallet.skills

import cm.aptoide.skills.SkillsViewModel
import cm.aptoide.skills.factory.TicketApiFactory
import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.repository.TicketsRepository
import cm.aptoide.skills.usecase.CreateTicketUseCase
import com.appcoins.wallet.bdsbilling.WalletService
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import javax.inject.Named

@Module
class SkillsModule {

  @Provides
  fun providesWalletAddressObtainer(walletService: WalletService): WalletAddressObtainer {
    return DefaultWalletAddressObtainer(walletService)
  }

  @Provides
  fun providesSkillsViewModel(createTicketUseCase: CreateTicketUseCase): SkillsViewModel {
    return SkillsViewModel(createTicketUseCase)
  }

  @Provides
  fun providesCreateTicketUseCase(walletAddressObtainer: WalletAddressObtainer,
                                  ticketsRepository: TicketsRepository): CreateTicketUseCase {
    return CreateTicketUseCase(walletAddressObtainer, ticketsRepository)
  }

  @Provides
  fun providesTicketsRepository(@Named("default") client: OkHttpClient): TicketsRepository {
    val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm")
        .create()

    return TicketsRepository(
        TicketApiFactory.providesTicketApi(client, gson))
  }
}