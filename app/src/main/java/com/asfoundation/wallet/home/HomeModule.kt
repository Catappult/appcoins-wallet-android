package com.asfoundation.wallet.home

import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.interact.TransactionViewInteractor
import com.asfoundation.wallet.main.MainActivityNavigator
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.ui.AppcoinsApps
import com.asfoundation.wallet.util.CurrencyFormatUtils
import dagger.Module
import dagger.Provides

@Module
class HomeModule {

  @Provides
  fun providesHomeNavigator(fragment: HomeFragment): HomeNavigator {
    return HomeNavigator(fragment, MainActivityNavigator(fragment.requireActivity()))
  }

  @Provides
  fun provideMainActivityNavigator(fragment: HomeFragment): MainActivityNavigator {
    return MainActivityNavigator(fragment.requireActivity())
  }

  @Provides
  fun providesHomeViewModelFactory(appcoinsApps: AppcoinsApps,
                                   homeAnalytics: HomeAnalytics,
                                   transactionViewInteractor: TransactionViewInteractor,
                                   walletsEventSender: WalletsEventSender,
                                   supportInteractor: SupportInteractor,
                                   currencyFormatUtils: CurrencyFormatUtils): HomeViewModelFactory {
    return HomeViewModelFactory(
        appcoinsApps, homeAnalytics, transactionViewInteractor, walletsEventSender,
        supportInteractor, currencyFormatUtils)
  }
}