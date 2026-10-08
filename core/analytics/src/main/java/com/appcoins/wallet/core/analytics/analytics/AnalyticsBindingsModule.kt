package com.appcoins.wallet.core.analytics.analytics

import com.appcoins.wallet.core.analytics.analytics.gamification.GamificationAnalytics
import com.appcoins.wallet.core.analytics.analytics.gamification.GamificationEventSender
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.analytics.analytics.legacy.EventSender
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsAnalytics
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsEventSender
import com.appcoins.wallet.core.analytics.analytics.partners.AddressService
import com.appcoins.wallet.core.analytics.analytics.partners.InstallerService
import com.appcoins.wallet.core.analytics.analytics.partners.InstallerSourceService
import com.appcoins.wallet.core.analytics.analytics.partners.PartnerAddressService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsBindingsModule {

  @Binds
  @Singleton
  abstract fun bindAnalyticsSetup(impl: IndicativeAnalytics): AnalyticsSetup

  @Binds
  abstract fun bindGamificationEventSender(impl: GamificationAnalytics): GamificationEventSender

  @Binds
  abstract fun bindEventSender(impl: BillingAnalytics): EventSender

  @Binds
  abstract fun bindWalletsEventSender(impl: WalletsAnalytics): WalletsEventSender

  @Binds
  abstract fun bindInstallerService(impl: InstallerSourceService): InstallerService

  @Binds
  abstract fun bindAddressService(impl: PartnerAddressService): AddressService
}