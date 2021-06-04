package com.asfoundation.wallet.home

import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.interact.TransactionViewInteractor
import com.asfoundation.wallet.navigator.TransactionViewNavigator
import com.asfoundation.wallet.promotions.ui.HomeNavigator
import com.asfoundation.wallet.router.*
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.transactions.TransactionsAnalytics
import com.asfoundation.wallet.ui.AppcoinsApps
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.viewmodel.TransactionsViewModelFactory
import dagger.Module
import dagger.Provides

@Module
class HomeModule {

  @Provides
  fun providesHomeNavigator(fragment: HomeFragment): HomeNavigator {
    return HomeNavigator(fragment)
  }

  @Provides
  fun providesTransactionsViewModelFactory(appcoinsApps: AppcoinsApps,
                                           transactionsAnalytics: TransactionsAnalytics,
                                           transactionsViewNavigator: TransactionViewNavigator,
                                           transactionViewInteractor: TransactionViewInteractor,
                                           walletsEventSender: WalletsEventSender,
                                           supportInteractor: SupportInteractor,
                                           currencyFormatUtils: CurrencyFormatUtils): TransactionsViewModelFactory {
    return TransactionsViewModelFactory(
        appcoinsApps, transactionsAnalytics,
        transactionsViewNavigator, transactionViewInteractor, walletsEventSender,
        supportInteractor, currencyFormatUtils)
  }

  @Provides
  fun provideTransactionsViewNavigator(sendRouter: SendRouter,
                                       transactionDetailRouter: TransactionDetailRouter,
                                       myAddressRouter: MyAddressRouter,
                                       balanceRouter: BalanceRouter,
                                       externalBrowserRouter: ExternalBrowserRouter,
                                       topUpRouter: TopUpRouter): TransactionViewNavigator {
    return TransactionViewNavigator(sendRouter, transactionDetailRouter,
        myAddressRouter, balanceRouter, externalBrowserRouter, topUpRouter)
  }

  @Provides
  fun provideSendRouter() = SendRouter()

  @Provides
  fun provideSendRouterTopUpRouter() = TopUpRouter()

  @Provides
  fun provideTransactionDetailRouter() = TransactionDetailRouter()

  @Provides
  fun provideMyAddressRouter() = MyAddressRouter()

  @Provides
  fun provideMyTokensRouter() = BalanceRouter()

  @Provides
  fun provideExternalBrowserRouter() = ExternalBrowserRouter()

  @Provides
  fun provideAirdropRouter() = AirdropRouter()
}