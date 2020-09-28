package com.asfoundation.wallet.di

import com.asfoundation.wallet.advertise.AdvertisingService
import com.asfoundation.wallet.advertise.WalletPoAService
import com.asfoundation.wallet.transactions.PerkBonusService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilders {

  @ContributesAndroidInjector
  abstract fun bindWalletPoAService(): WalletPoAService

  @ContributesAndroidInjector
  abstract fun bindPerkBonusService(): PerkBonusService

  @ActivityScope
  @ContributesAndroidInjector
  abstract fun bindAdvertisingService(): AdvertisingService
}
