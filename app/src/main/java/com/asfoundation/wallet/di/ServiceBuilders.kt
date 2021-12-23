package com.asfoundation.wallet.di

import cm.aptoide.skills.games.BackgroundGameService
import com.asfoundation.wallet.advertise.AdvertisingService
import com.asfoundation.wallet.advertise.WalletPoAService
import com.asfoundation.wallet.advertise.WalletPoAServiceModule
import com.asfoundation.wallet.eskills.di.SkillsModule
import com.asfoundation.wallet.transactions.PerkBonusAndGamificationService
import com.asfoundation.wallet.transactions.PerkBonusAndGamificationServiceModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilders {

  @ContributesAndroidInjector(modules = [WalletPoAServiceModule::class])
  abstract fun bindWalletPoAService(): WalletPoAService

  @ContributesAndroidInjector(modules = [PerkBonusAndGamificationServiceModule::class])
  abstract fun bindPerkBonusAndGamificationService(): PerkBonusAndGamificationService

  @ActivityScope
  @ContributesAndroidInjector
  abstract fun bindAdvertisingService(): AdvertisingService

  @ActivityScope
  @ContributesAndroidInjector(modules = [SkillsModule::class])
  abstract fun bindBackgroundGameService(): BackgroundGameService
}
