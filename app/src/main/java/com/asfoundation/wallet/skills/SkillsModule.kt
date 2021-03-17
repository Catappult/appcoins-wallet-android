package com.asfoundation.wallet.skills

import cm.aptoide.skills.SkillsViewModel
import cm.aptoide.skills.WalletAddressObtainer
import cm.aptoide.skills.factory.TicketApiFactory
import cm.aptoide.skills.repository.TicketsRepository
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
  fun providesSkillsViewModel(ticketsRepository: TicketsRepository): SkillsViewModel {
    return SkillsViewModel(ticketsRepository)
  }

  @Provides
  fun providesTicketsRepository(@Named("default") client: OkHttpClient,
                                walletAddressObtainer: WalletAddressObtainer): TicketsRepository {
    val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm")
        .create()

    return TicketsRepository(walletAddressObtainer,
        TicketApiFactory.providesTicketApi(client, gson))
  }
}